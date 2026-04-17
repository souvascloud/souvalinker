package com.souvanik.souvalinker.service;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface TokenService {
    String generateJwt(Long userId);

    boolean validateJwt(String token);

    String generateVerificationToken();

    String generatePasswordResetToken();

    Long extractUserId(String token);

}
