
package com.healthskillconnect.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.healthskillconnect.model.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt-secret}") String secret,
            @Value("${app.jwt-expiration-minutes:60}") long expirationMinutes) {

        if (secret == null ||
                secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 bytes long."
            );
        }

        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMinutes = expirationMinutes;
    }

    public String create(AppUser user) {
        if (user == null || user.id == null || user.role == null) {
            throw new IllegalArgumentException(
                    "User, user ID, and role are required."
            );
        }

        Date issuedAt = new Date();
        Date expiresAt = new Date(
                issuedAt.getTime() + expirationMinutes * 60_000L
        );

        return Jwts.builder()
                .subject(user.id)
                .claim("role", user.role)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public Claims parse(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT is missing.");
        }

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
