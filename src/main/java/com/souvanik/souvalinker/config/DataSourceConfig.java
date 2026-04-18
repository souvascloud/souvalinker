package com.souvanik.souvalinker.config;

import com.souvanik.souvalinker.config.properties.AppProperties;
import com.souvanik.souvalinker.config.properties.PrimaryDbProperties;
import com.souvanik.souvalinker.config.properties.ReplicaDbProperties;
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

    private final PrimaryDbProperties primaryDbProperties;
    private final ReplicaDbProperties replicaDbProperties;

    @Bean
    public DataSource primaryDataSource() {

        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(primaryDbProperties.url());

        ds.setUsername(primaryDbProperties.username());

        ds.setPassword(primaryDbProperties.password());

        /*
        Pool settings
       */
        ds.setMaximumPoolSize(20);

        ds.setMinimumIdle(5);

        ds.setConnectionTimeout(30000);

        ds.setIdleTimeout(600000);

        ds.setMaxLifetime(1800000);

        return ds;
    }


    @Bean
    public DataSource replicaDataSource() {

        HikariDataSource ds = new HikariDataSource();

        ds.setJdbcUrl(replicaDbProperties.url());

        ds.setUsername(replicaDbProperties.username());

        ds.setPassword(replicaDbProperties.password());
        /*
        Bigger read pool
       */
        ds.setMaximumPoolSize(40);

        ds.setMinimumIdle(10);

        ds.setConnectionTimeout(30000);

        ds.setIdleTimeout(600000);

        ds.setMaxLifetime(1800000);

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