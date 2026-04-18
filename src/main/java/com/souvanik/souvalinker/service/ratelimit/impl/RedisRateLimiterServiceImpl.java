package com.souvanik.souvalinker.service.ratelimit.impl;

import com.souvanik.souvalinker.service.ratelimit.RedisRateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Service
@RequiredArgsConstructor
public class RedisRateLimiterServiceImpl implements RedisRateLimiterService {

    private final RedisTemplate<String,String> redisTemplate;


    @Override
    public boolean allowRequest(String key, long limit, Duration window) {

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) return false;

        /*
         First request:
         set TTL
         */
        if (count == 1) {
            redisTemplate.expire(key, window);
        }

        return count <= limit;
    }
}