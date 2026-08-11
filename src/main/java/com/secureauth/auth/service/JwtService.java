package com.secureauth.auth.service;

import com.secureauth.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(User user) {
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("USER");

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaims(token).get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaims(token).get("type", String.class));
    }
}