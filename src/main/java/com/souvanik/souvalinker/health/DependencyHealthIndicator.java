package com.souvanik.souvalinker.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
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
public class DependencyHealthIndicator implements HealthIndicator {

    private final DataSource primaryDataSource;

    private final DataSource replicaDataSource;

    private final RedisTemplate<String,String> redisTemplate;


    @Override
    public Health health() {

        boolean primaryUp = checkDatabase(primaryDataSource);

        boolean replicaUp = checkDatabase(replicaDataSource);

        boolean redisUp = checkRedis();


        if (primaryUp && replicaUp && redisUp) {

            return Health.up().withDetail("primaryDb", "UP")
                    .withDetail("replicaDb", "UP")
                    .withDetail("redis", "UP")
                    .build();
        }


        return Health.down()
                .withDetail("primaryDb", primaryUp ? "UP" : "DOWN")
                .withDetail("replicaDb", replicaUp ? "UP" : "DOWN")
                .withDetail("redis", redisUp ? "UP" : "DOWN")
                .build();
    }



    private boolean checkDatabase(DataSource dataSource) {

        try (Connection connection = dataSource.getConnection()) {

            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }



    private boolean checkRedis() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception ex) {
            return false;
        }
    }
}
