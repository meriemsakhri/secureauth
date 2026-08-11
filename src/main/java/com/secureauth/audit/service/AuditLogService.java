package com.secureauth.audit.service;

import com.secureauth.audit.model.AuditEventType;
import com.secureauth.audit.model.AuditLog;
import com.secureauth.audit.repository.AuditLogRepository;
import com.secureauth.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User user, AuditEventType eventType, String ipAddress, String details) {
        AuditLog entry = AuditLog.builder()
                .user(user)
                .eventType(eventType)
                .ipAddress(ipAddress)
                .details(details)
                .build();

        auditLogRepository.save(entry);
    }
}