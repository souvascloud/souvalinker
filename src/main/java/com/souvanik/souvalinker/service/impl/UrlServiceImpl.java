package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.config.AppProperties;
import com.souvanik.souvalinker.dto.payload.ShortUrlPayload;
import com.souvanik.souvalinker.dto.request.CreateShortUrlRequest;
import com.souvanik.souvalinker.entity.UrlMapping;
import com.souvanik.souvalinker.entity.User;
import com.souvanik.souvalinker.exception.ResourceNotFoundException;
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



    @Override
    public ShortUrlPayload createShortUrl(CreateShortUrlRequest request, Long userId) {

        logger.info("Creating short URL for longUrl={}", request.longUrl());

        UrlMapping urlMapping = new UrlMapping();

        urlMapping.setLongUrl(request.longUrl());

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> {
                            logger.error("User not found userId={}", userId);
                            return new ResourceNotFoundException("User not found");
                        });

        urlMapping.setUser(user);


        UrlMapping saved = urlMappingRepository.save(urlMapping);


        String shortCode = shortCodeGenerator.generate(saved.getId());


        saved.setShortCode(shortCode);

        urlMappingRepository.save(saved);


        // cache warm-up
        String cacheKey = CACHE_PREFIX + shortCode;

        redisTemplate.opsForValue().set(
                        cacheKey,
                        saved.getLongUrl(),
                        CACHE_TTL
                );


        String fullShortUrl = appProperties.shortUrl().baseUrl() + "/" + shortCode;

        logger.info("Short URL created successfully shortCode={}", shortCode);


        return new ShortUrlPayload(fullShortUrl);

    }


    @Override
    @Transactional(readOnly = true)
    public String resolveOriginalUrl(String shortCode) {

        logger.info("Resolving shortCode={}", shortCode);

        String cacheKey = CACHE_PREFIX + shortCode;


        // 1 cache lookup
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            logger.info("Cache hit for shortCode={}", shortCode);
            return cachedUrl;
        }

        logger.info("Cache miss for shortCode={}", shortCode);


        // 2 db lookup
        UrlMapping urlMapping =
                urlMappingRepository
                        .findByShortCode(shortCode)
                        .orElseThrow(() -> {

                            logger.error(
                                    "Short URL not found shortCode={}",
                                    shortCode
                            );

                            return new ResourceNotFoundException(
                                    "Short URL not found"
                            );
                        });


        // 3 populate cache
        redisTemplate.opsForValue().set(
                        cacheKey,
                        urlMapping.getLongUrl(),
                        CACHE_TTL
                );

        logger.info("Short URL resolved from DB and cached shortCode={}", shortCode);

        return urlMapping.getLongUrl();
    }
}
