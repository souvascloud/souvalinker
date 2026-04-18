package com.souvanik.souvalinker.strategy;

import jakarta.servlet.http.HttpServletRequest;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface RateLimitStrategy {
    String buildKey(HttpServletRequest request);
}
