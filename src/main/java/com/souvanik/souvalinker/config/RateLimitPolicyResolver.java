package com.souvanik.souvalinker.config;

import com.souvanik.souvalinker.model.RateLimitPolicy;
import com.souvanik.souvalinker.model.RateLimitType;
import org.springframework.stereotype.Component;

import java.time.Duration;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
public class RateLimitPolicyResolver {

    public RateLimitPolicy resolve(RateLimitType type) {

        return switch (type) {

            case LOGIN_IP ->
                    new RateLimitPolicy(
                            10,
                            Duration.ofMinutes(1)
                    );

            case URL_CREATE_USER ->
                    new RateLimitPolicy(
                            100,
                            Duration.ofDays(1)
                    );

            default -> throw new IllegalArgumentException("Unsupported policy");
        };
    }
}