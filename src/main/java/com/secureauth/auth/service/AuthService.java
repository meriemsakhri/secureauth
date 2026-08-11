package com.secureauth.auth.service;

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
import org.springframework.beans.factory.annotation.Value;

import com.secureauth.audit.model.AuditEventType;
import com.secureauth.audit.service.AuditLogService;

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

    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Transactional
    public AuthResponse signup(SignupRequest request, String ipAddress) {        if (userRepository.existsByEmail(request.getEmail())) {
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
    public AuthResponse login(LoginRequest request, String ipAddress) {        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            auditLogService.log(user, AuditEventType.LOGIN_FAIL, ipAddress, "Attempt on locked account");
            throw new AccountLockedException("Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, ipAddress);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        auditLogService.log(user, AuditEventType.LOGIN_SUCCESS, ipAddress, null);

        return issueTokens(user);
    }

    private void handleFailedLogin(User user, String ipAddress) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(lockoutDurationMinutes));
            auditLogService.log(user, AuditEventType.ACCOUNT_LOCKED, ipAddress,
                    "Locked after " + attempts + " failed attempts");
        } else {
            auditLogService.log(user, AuditEventType.LOGIN_FAIL, ipAddress,
                    "Failed attempt " + attempts + " of " + maxFailedAttempts);
        }

        userRepository.save(user);
    }
}