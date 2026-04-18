package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.config.properties.AppProperties;
import com.souvanik.souvalinker.service.TokenService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class TokenServiceImpl  implements TokenService {


    private final AppProperties appProperties;
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);



    @Override
    public String generateJwt(Long userId) {

        logger.debug("event=jwt_generation_started userId={}", userId);

        String jwt =
                Jwts.builder()
                        .setSubject(
                                String.valueOf(userId)
                        )
                        .setIssuedAt(
                                new Date()
                        )
                        .setExpiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + appProperties
                                                .jwt()
                                                .expirationMs()
                                )
                        )
                        .signWith(
                                SignatureAlgorithm.HS256,
                                appProperties.jwt().secret()
                        )
                        .compact();


        logger.debug("event=jwt_generation_success userId={}", userId);

        return jwt;
    }
    @Override
    public boolean validateJwt(String token) {

        try {

            Jwts.parser()
                    .setSigningKey(
                            appProperties.jwt().secret()
                    )
                    .parseClaimsJws(
                            token
                    );

            return true;

        } catch (ExpiredJwtException ex) {

            logger.warn("event=jwt_validation_failed reason=expired");

            return false;

        } catch (MalformedJwtException ex) {

            logger.warn("event=jwt_validation_failed reason=malformed");

            return false;

        } catch (SignatureException ex) {

            logger.warn("event=jwt_validation_failed reason=bad_signature");

            return false;

        } catch (Exception ex) {

            logger.error("event=jwt_validation_failed reason=unexpected", ex);

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
    public Long extractUserId(String token) {

        try {

            Claims claims =
                    Jwts.parser()
                            .setSigningKey(
                                    appProperties.jwt().secret()
                            )
                            .parseClaimsJws(
                                    token
                            )
                            .getBody();

            return Long.valueOf(
                    claims.getSubject()
            );

        } catch (Exception ex) {

            logger.warn("event=jwt_user_extract_failed");

            throw ex;
        }
    }
}
