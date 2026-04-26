package com.souvanik.souvalinker.service;

import java.util.Date;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface TokenService {

    String generateAccessToken(Long userId);

    boolean isTokenValid(String token);

    Long extractUserId(String token);

    String extractJti(String token);

    Date extractExpiration(String token);



    String generateRefreshToken();


    String generateVerificationToken();

    String generatePasswordResetToken();

    String hashToken(String token);
}
