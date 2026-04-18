package com.souvanik.souvalinker.strategy;

import com.souvanik.souvalinker.annotation.RateLimited;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Component
public class IpRateLimitStrategy  implements RateLimitStrategy {
    @Override
    public String buildKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return "rate_limit:ip:" + ip;
    }
}
