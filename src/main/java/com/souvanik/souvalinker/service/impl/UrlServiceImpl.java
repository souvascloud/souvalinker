package com.souvanik.souvalinker.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.souvanik.souvalinker.annotation.ReadOnlyReplica;
import com.souvanik.souvalinker.config.properties.AppProperties;
import com.souvanik.souvalinker.dto.payload.ShortUrlPayload;
import com.souvanik.souvalinker.dto.request.CreateShortUrlRequest;
import com.souvanik.souvalinker.entity.UrlMapping;
import com.souvanik.souvalinker.entity.User;
import com.souvanik.souvalinker.exception.ResourceNotFoundException;
import com.souvanik.souvalinker.metric.UrlMetricsService;
import com.souvanik.souvalinker.repository.UrlMappingRepository;
import com.souvanik.souvalinker.repository.UserRepository;
import com.souvanik.souvalinker.service.UrlService;
import com.souvanik.souvalinker.strategy.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UrlServiceImpl implements UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlServiceImpl.class);

    private static final String CACHE_PREFIX = "short-url:";

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final UrlMappingRepository urlMappingRepository;

    private final UserRepository userRepository;

    private final ShortCodeGenerator shortCodeGenerator;

    private final AppProperties appProperties;

    private final RedisTemplate<String,String> redisTemplate;

    private final Cache<String,String> urlCache;

    private final UrlMetricsService metricsService;



    @Override
    public ShortUrlPayload createShortUrl(CreateShortUrlRequest request, Long userId) {

        logger.info("event=short_url_create_started userId={}", userId);


        UrlMapping urlMapping = new UrlMapping();

        urlMapping.setLongUrl(request.longUrl());


        User user =
                userRepository.findById(
                                userId
                        )
                        .orElseThrow(() -> {
                            logger.warn("event=short_url_create_rejected reason=user_not_found userId={}", userId);
                            return new ResourceNotFoundException("User not found");
                        });

        urlMapping.setUser(user);

        UrlMapping saved = urlMappingRepository.save(urlMapping);


        String shortCode = shortCodeGenerator.generate(saved.getId());

        saved.setShortCode(shortCode);


        urlMappingRepository.save(saved);


        String cacheKey = CACHE_PREFIX + shortCode;


        // L2 warm-up (Redis)
        redisTemplate.opsForValue().set(cacheKey, saved.getLongUrl(), CACHE_TTL);


        // L1 warm-up (Caffeine)
        urlCache.put(shortCode, saved.getLongUrl());


        String fullShortUrl = appProperties.shortUrl().baseUrl() + "/" + shortCode;


        logger.info("event=short_url_create_success userId={} shortCode={}", userId, shortCode);

        metricsService.incrementShortUrlCreated();

        return new ShortUrlPayload(fullShortUrl);
    }


    @Override
    @Transactional(readOnly = true)
    @ReadOnlyReplica
    public String resolveOriginalUrl(String shortCode) {

        long start = System.currentTimeMillis();

        logger.info("event=url_resolve_started shortCode={}", shortCode);

        /*
          1. L1 Cache (Caffeine)
         */
        String localCachedUrl = urlCache.getIfPresent(shortCode);

        if (localCachedUrl != null) {
            logger.info("event=url_resolve_l1_cache_hit shortCode={}", shortCode);
            metricsService.incrementRedirectResolution("l1");
            metricsService.recordRedirectLatency(
                    System.currentTimeMillis() - start
            );
            return localCachedUrl;
        }

        String cacheKey = CACHE_PREFIX + shortCode;

        /*
          2. L2 Cache (Redis)
         */
        String redisCachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (redisCachedUrl != null) {
            logger.info("event=url_resolve_l2_cache_hit shortCode={}", shortCode);
            /*
             Promote L2 -> L1
             */
            urlCache.put(shortCode, redisCachedUrl);
            metricsService.incrementRedirectResolution("l2");
            metricsService.recordRedirectLatency(System.currentTimeMillis() - start);
            return redisCachedUrl;
        }

        logger.info("event=url_resolve_cache_miss shortCode={}", shortCode);
        /*
          3. DB Fallback (Read Replica)
         */
        UrlMapping urlMapping =
                urlMappingRepository
                        .findByShortCode(
                                shortCode
                        )
                        .orElseThrow(() -> {
                            logger.warn("event=url_resolve_rejected reason=not_found shortCode={}", shortCode);
                            return new ResourceNotFoundException("Short URL not found");
                        });

    /*
      Populate Redis (L2)
     */
        redisTemplate.opsForValue().set(cacheKey, urlMapping.getLongUrl(), CACHE_TTL);
    /*
      Populate Caffeine (L1)
     */
        urlCache.put(shortCode, urlMapping.getLongUrl());

        logger.info("event=url_resolve_db_hit_cached shortCode={}", shortCode);

        metricsService.incrementRedirectResolution("db");
        metricsService.recordRedirectLatency(System.currentTimeMillis() - start);

        return urlMapping.getLongUrl();
    }
}
