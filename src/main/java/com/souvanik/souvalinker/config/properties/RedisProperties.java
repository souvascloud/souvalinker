package com.souvanik.souvalinker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProperties(

        String host,
        int port

) {}