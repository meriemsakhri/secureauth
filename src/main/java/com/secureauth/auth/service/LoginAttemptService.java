package com.secureauth.auth.service;

import com.secureauth.audit.model.AuditEventType;
import com.secureauth.audit.service.AuditLogService;
import com.secureauth.auth.model.User;
import com.secureauth.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(User user, String ipAddress) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLockedAttempt(User user, String ipAddress) {
        auditLogService.log(user, AuditEventType.LOGIN_FAIL, ipAddress, "Attempt on locked account");
    }
}