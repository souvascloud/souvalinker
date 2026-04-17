package com.souvanik.souvalinker.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {


    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {

            if (connection.isValid(2)) {

                return Health.up().withDetail("database", "available").build();
            }

            return Health.down().withDetail("database", "invalid connection").build();

        } catch (Exception ex) {

            return Health.down(ex).withDetail("database", "unavailable").build();
        }
    }
}
