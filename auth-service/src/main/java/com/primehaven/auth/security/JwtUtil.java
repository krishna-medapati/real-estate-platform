package com.primehaven.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Shared JWT secret across all PrimeHaven services so that a token issued
 * here by auth-service can be validated by property-service, visit-service
 * and inquiry-service without a shared library.
 *
 * IMPORTANT: keep JWT_SECRET identical in every service's application.yml.
 */
@Component
public class JwtUtil {

    private final String jwtSecret;

    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
}
