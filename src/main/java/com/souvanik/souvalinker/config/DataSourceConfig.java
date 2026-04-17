package com.souvanik.souvalinker.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final AppProperties appProperties;


    @Bean
    public DataSource primaryDataSource() {

        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(appProperties.database().primary().url());

        ds.setUsername(appProperties.database().primary().username());

        ds.setPassword(appProperties.database().primary().password());

        return ds;
    }


    @Bean
    public DataSource replicaDataSource() {

        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(appProperties.database().replica().url());

        ds.setUsername(appProperties.database().replica().username());

        ds.setPassword(appProperties.database().replica().password());

        return ds;
    }


    @Primary
    @Bean
    public DataSource routingDataSource(DataSource primaryDataSource, DataSource replicaDataSource) {

        Map<Object, Object> targets = new HashMap<>();

        targets.put("PRIMARY", primaryDataSource);

        targets.put("REPLICA", replicaDataSource);

        RoutingDataSource routing = new RoutingDataSource();

        routing.setDefaultTargetDataSource(primaryDataSource);

        routing.setTargetDataSources(targets);

        routing.afterPropertiesSet();

        return routing;
    }
}