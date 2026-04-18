package com.souvanik.souvalinker.config;

import com.souvanik.souvalinker.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    private final AppProperties appProperties;

    @Bean
    public SesV2Client sesV2Client() {
        return SesV2Client.builder()
                .region(Region.of(appProperties.aws().region()))
                .build();
    }
}
