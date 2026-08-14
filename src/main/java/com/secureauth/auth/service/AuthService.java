package com.secureauth.auth.service;

import com.secureauth.auth.dto.RefreshTokenRequest;
import com.secureauth.auth.exception.InvalidTokenException;
import io.jsonwebtoken.JwtException;

import com.secureauth.auth.dto.AuthResponse;
import com.secureauth.auth.dto.SignupRequest;
import com.secureauth.auth.exception.EmailAlreadyExistsException;
import com.secureauth.auth.model.RefreshToken;
import com.secureauth.auth.model.Role;
import com.secureauth.auth.model.User;
import com.secureauth.auth.repository.RefreshTokenRepository;
import com.secureauth.auth.repository.RoleRepository;
import com.secureauth.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secureauth.auth.dto.LoginRequest;
import com.secureauth.auth.exception.AccountLockedException;
import com.secureauth.auth.exception.InvalidCredentialsException;

import com.secureauth.audit.model.AuditEventType;
import com.secureauth.audit.service.AuditLogService;

import com.secureauth.auth.dto.ForgotPasswordRequest;
import com.secureauth.auth.dto.ResetPasswordRequest;
import com.secureauth.auth.model.PasswordResetToken;
import com.secureauth.auth.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import java.security.SecureRandom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.security.password-reset-expiration-minutes}")
    private int passwordResetExpirationMinutes;

    @Transactional
    public AuthResponse signup(SignupRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role not found — check V2 migration"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .failedAttempts(0)
                .roles(roles)
                .build();

        userRepository.save(user);
        auditLogService.log(user, AuditEventType.SIGNUP_SUCCESS, ipAddress, "New account registered");
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(tokenEntity);

        String role = user.getRoles().stream().findFirst().map(Role::getName).orElse("USER");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(role)
                .build();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            loginAttemptService.recordLockedAttempt(user, ipAddress);
            throw new AccountLockedException("Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.recordFailedAttempt(user, ipAddress);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        auditLogService.log(user, AuditEventType.LOGIN_SUCCESS, ipAddress, null);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();

        try {
            if (jwtService.isTokenExpired(rawToken) || !jwtService.isRefreshToken(rawToken)) {
                throw new InvalidTokenException("Invalid or expired refresh token");
            }
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String tokenHash = hashToken(rawToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not recognized"));

        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String rawToken = generateSecureToken();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(hashToken(rawToken))
                    .expiresAt(OffsetDateTime.now().plusMinutes(passwordResetExpirationMinutes))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            auditLogService.log(user, AuditEventType.PASSWORD_RESET_REQUESTED, ipAddress, null);

            // MOCKED EMAIL DELIVERY — logs the reset link instead of sending a real email.
            // Replace with a real mail provider (SMTP, SendGrid, etc.) for production.
            System.out.println("=== PASSWORD RESET LINK (mocked email) ===");
            System.out.println("To: " + user.getEmail());
            System.out.println("Reset token: " + rawToken);
            System.out.println("Expires in " + passwordResetExpirationMinutes + " minutes");
            System.out.println("===========================================");
        });
        // Always returns silently, whether the email existed or not — prevents user enumeration.
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("This reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditLogService.log(user, AuditEventType.PASSWORD_RESET_SUCCESS, null, null);
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}