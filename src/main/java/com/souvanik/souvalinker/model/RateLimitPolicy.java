package com.souvanik.souvalinker.model;

import java.time.Duration;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public record RateLimitPolicy(

        long limit,

        Duration window

) {}