package com.souvanik.souvalinker.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
public class UserRateLimitStrategy implements RateLimitStrategy {

    @Override
    public String buildKey(HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Long userId = Long.valueOf(auth.getName());

        return "rate_limit:user:" + userId;
    }
}
