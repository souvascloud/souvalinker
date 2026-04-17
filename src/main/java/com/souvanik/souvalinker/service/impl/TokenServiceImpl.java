package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Service
public class TokenServiceImpl  implements TokenService {

    // TODO externalize later
    private static final String JWT_SECRET =
            "replace-this-secret-later";

    // TODO externalize later
    private static final long JWT_EXPIRATION_MS =
            3600000;


    @Override
    public String generateJwt(Long userId) {

        return Jwts.builder()
                .setSubject(
                        String.valueOf(userId)
                )
                .setIssuedAt(
                        new Date()
                )
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + JWT_EXPIRATION_MS
                        )
                )
                .signWith(
                        SignatureAlgorithm.HS256,
                        JWT_SECRET
                )
                .compact();
    }


    @Override
    public boolean validateJwt(
            String token) {

        try {

            Jwts.parser()
                    .setSigningKey(
                            JWT_SECRET
                    )
                    .parseClaimsJws(token);

            return true;

        } catch (Exception ex) {

            return false;
        }
    }


    @Override
    public String generateVerificationToken() {

        return UUID
                .randomUUID()
                .toString();
    }


    @Override
    public String generatePasswordResetToken() {

        return UUID
                .randomUUID()
                .toString();
     }

    @Override
    public Long extractUserId(
            String token) {

        Claims claims =
                Jwts.parser()
                        .setSigningKey(
                                JWT_SECRET
                        )
                        .parseClaimsJws(token)
                        .getBody();

        return Long.valueOf(
                claims.getSubject()
        );
    }

}
