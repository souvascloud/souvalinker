package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.config.properties.AppProperties;
import com.souvanik.souvalinker.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

        private final AppProperties appProperties;

        private Key getSigningKey() {
            byte[] keyBytes = Base64.getDecoder().decode(appProperties.jwt().secret());
            return Keys.hmacShaKeyFor(keyBytes);
        }

        @Override
        public String generateAccessToken(Long userId) {

            Date now = new Date();
            Date expiry = new Date(now.getTime() + appProperties.jwt().expirationMs());

            return Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setId(UUID.randomUUID().toString())
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        }

        @Override
        public boolean isTokenValid(String token) {
            try {
                Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(token);
                return true;
            } catch (JwtException | IllegalArgumentException ex) {
                return false;
            }
        }

        @Override
        public Long extractUserId(String token) {
            return Long.valueOf(extractAllClaims(token).getSubject());
        }

        @Override
        public String extractJti(String token) {
            return extractAllClaims(token).getId();
        }

        @Override
        public Date extractExpiration(String token) {
            return extractAllClaims(token).getExpiration();
        }

        private Claims extractAllClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }


        @Override
        public String generateRefreshToken() {
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        }

        @Override
        public String generateVerificationToken() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String generatePasswordResetToken() {
            return UUID.randomUUID().toString();
        }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(2 * bytes.length);

        for (byte b : bytes) {
            String hexChar = Integer.toHexString(0xff & b);
            if (hexChar.length() == 1) {
                hex.append('0');
            }
            hex.append(hexChar);
        }

        return hex.toString();
    }
}