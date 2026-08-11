package com.secureauth.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserSummaryDto {
    private UUID id;
    private String email;
    private boolean enabled;
    private int failedAttempts;
}