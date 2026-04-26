package com.souvanik.souvalinker.dto.payload;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public record AuthPayload(
        String accessToken,
        String refreshToken,
        String tokenType
) {}