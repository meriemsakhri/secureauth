package com.secureauth.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String message;
}