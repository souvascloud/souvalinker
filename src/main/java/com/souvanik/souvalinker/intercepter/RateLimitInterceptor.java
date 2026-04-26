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

        //  Only handle controller methods
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        //  Check annotation
        RateLimited rateLimited = method.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            return true;
        }

        // Resolve type + policy
        RateLimitType type = rateLimited.strategy();
        RateLimitPolicy policy = policyResolver.resolve(type);

        // Build key based on strategy
        String rawKey;

        switch (type) {

            //  IP-based endpoints
            case REGISTER_IP,
                 LOGIN_IP,
                 FORGOT_PASSWORD_IP,RESET_PASSWORD_IP -> rawKey = ipStrategy.buildKey(request);

            // Refresh token-based
            case REFRESH_TOKEN -> {
                Object tokenAttr = request.getAttribute("refreshToken");
                rawKey = (tokenAttr != null) ? tokenAttr.toString() : "unknown";
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported rate limit type: " + type
            );
        }

        //  Redis key (namespaced)
        String key = "ratelimit:" + type.name() + ":" + rawKey;

        //  Call Redis limiter
        boolean allowed = rateLimiterService.allowRequest(
                key,
                policy.limit(),
                policy.window()
        );

        // Handle rejection
        if (!allowed) {
            metricsService.incrementRateLimitRejection(type.name());
            throw new RateLimitExceededException("Rate limit exceeded");
        }

        //  Track success (optional but useful)
        metricsService.incrementRateLimitSuccess(type.name());

        return true;
    }
}