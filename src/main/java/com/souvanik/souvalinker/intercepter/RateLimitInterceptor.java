package com.souvanik.souvalinker.intercepter;

import com.souvanik.souvalinker.annotation.RateLimited;
import com.souvanik.souvalinker.config.RateLimitPolicyResolver;
import com.souvanik.souvalinker.exception.RateLimitExceededException;
import com.souvanik.souvalinker.metric.UrlMetricsService;
import com.souvanik.souvalinker.model.RateLimitPolicy;
import com.souvanik.souvalinker.model.RateLimitType;
import com.souvanik.souvalinker.service.ratelimit.RedisRateLimiterService;
import com.souvanik.souvalinker.strategy.IpRateLimitStrategy;
import com.souvanik.souvalinker.strategy.UserRateLimitStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitPolicyResolver policyResolver;

    private final RedisRateLimiterService rateLimiterService;

    private final IpRateLimitStrategy ipStrategy;

    private final UserRateLimitStrategy userStrategy;

    private final UrlMetricsService metricsService;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }


        RateLimited rateLimited = method.getMethodAnnotation(RateLimited.class);


        if (rateLimited == null) {return true;}


        RateLimitType type = rateLimited.strategy();


        RateLimitPolicy policy = policyResolver.resolve(type);

        String key;

        if (type == RateLimitType.URL_CREATE_USER) {
            key = userStrategy.buildKey(request);
        } else {
            key = ipStrategy.buildKey(request);
        }


        boolean allowed = rateLimiterService.allowRequest(
                        key,
                        policy.limit(),
                        policy.window()
                );

        if (!allowed) {
            metricsService.incrementRateLimitRejection(
                    type.name()
            );

            throw new RateLimitExceededException("Rate limit exceeded");
        }

        return true;
    }
}