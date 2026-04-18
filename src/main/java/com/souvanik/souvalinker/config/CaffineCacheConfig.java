package com.souvanik.souvalinker.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Configuration
public class CaffineCacheConfig {

    @Bean
    public Cache<String,String> urlCache() {

        return Caffeine.newBuilder()
                .maximumSize(100_000)
                // L1 TTL here
                .expireAfterWrite(
                        Duration.ofHours(1)
                )

                .build();
    }
}
