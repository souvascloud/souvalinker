package com.souvanik.souvalinker.service.ratelimit;

import java.time.Duration;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface RedisRateLimiterService {

    boolean allowRequest(String key, long limit, Duration window);
}