package com.souvanik.souvalinker.config;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    @Bean
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory factory) {

            RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();

            redisTemplate.setConnectionFactory(factory);

            StringRedisSerializer serializer = new StringRedisSerializer();

            redisTemplate.setKeySerializer(serializer);
            redisTemplate.setValueSerializer(serializer);

            redisTemplate.setHashKeySerializer(serializer);
            redisTemplate.setHashValueSerializer(serializer);

            redisTemplate.afterPropertiesSet();

            return redisTemplate;
        }
}
