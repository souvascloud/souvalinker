package com.souvanik.souvalinker.config;

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
public class AwsConfig {

    @Bean
    public SesV2Client sesV2Client() {

        return SesV2Client.builder()
                .region(
                        Region.AP_SOUTH_1
                )
                .build();
    }
}
