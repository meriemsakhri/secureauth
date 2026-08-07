-- V1: Initial schema for SecureAuth
-- Users, roles, RBAC join table, refresh tokens, and audit logging

CREATE TABLE users (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       enabled         BOOLEAN NOT NULL DEFAULT TRUE,
                       failed_attempts INT NOT NULL DEFAULT 0,
                       locked_until    TIMESTAMPTZ NULL,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE roles (
                       id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
                                id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash VARCHAR(255) NOT NULL,
                                expires_at TIMESTAMPTZ NOT NULL,
                                revoked    BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
                            id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id    UUID NULL REFERENCES users(id) ON DELETE SET NULL,
                            event_type VARCHAR(50) NOT NULL,
                            ip_address VARCHAR(45) NULL,
                            timestamp  TIMESTAMPTZ NOT NULL DEFAULT now(),
                            details    TEXT NULL
);

-- Indexes for common lookup patterns
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);