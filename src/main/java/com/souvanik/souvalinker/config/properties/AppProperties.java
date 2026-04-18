package com.souvanik.souvalinker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Auth auth,
        ShortUrl shortUrl,
        Mail mail,
        Aws aws

) {
    public record Jwt(
            String secret,
            long expirationMs
    ) {}
    public record Auth(
            long resetTokenExpiryMinutes,
            long verifyTokenExpiryHours
    ) {}
    public record ShortUrl(
            String baseUrl,
            int codeLength
    ) {}
    public record Mail(
            String fromEmail
    ) {}
    public record Aws(
            String region
    ) {}
}