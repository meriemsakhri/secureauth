package com.secureauth.audit.model;

public enum AuditEventType {
    SIGNUP_SUCCESS,
    LOGIN_SUCCESS,
    LOGIN_FAIL,
    ACCOUNT_LOCKED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_SUCCESS
}