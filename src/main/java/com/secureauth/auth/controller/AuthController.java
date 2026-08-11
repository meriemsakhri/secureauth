package com.secureauth.auth.controller;

import com.secureauth.auth.dto.AuthResponse;
import com.secureauth.auth.dto.LoginRequest;
import com.secureauth.auth.dto.SignupRequest;
import com.secureauth.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.signup(request, getClientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, getClientIp(httpRequest));
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}