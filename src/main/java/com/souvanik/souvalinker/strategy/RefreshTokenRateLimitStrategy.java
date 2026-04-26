package com.souvanik.souvalinker.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
public class RefreshTokenRateLimitStrategy implements RateLimitStrategy {

    @Override
    public String buildKey(HttpServletRequest request) {

        Object token = request.getAttribute("refreshToken");

        if (token == null) {
            return "unknown";
        }

        return token.toString();
    }
}