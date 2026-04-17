package com.souvanik.souvalinker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.sql.DataSource;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(ShortUrl shortUrl, Mail mail, Database database) {

    public record ShortUrl(String baseUrl) {}

    public record Mail(String fromEmail) {}


    public record Database(Db primary, Db replica) {}


    public record Db(String url, String username, String password) {}
}