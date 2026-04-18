package com.souvanik.souvalinker.config.properties;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource.primary")
public record PrimaryDbProperties(

        String url,
        String username,
        String password,
        String driverClassName

) {}