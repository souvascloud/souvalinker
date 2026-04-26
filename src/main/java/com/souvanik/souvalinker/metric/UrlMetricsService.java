package com.souvanik.souvalinker.metric;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface UrlMetricsService {
    void incrementShortUrlCreated();

    void incrementRedirectResolution(String source);

    void incrementRateLimitRejection(String endpoint);

    void recordRedirectLatency(long millis);

    public void incrementRateLimitSuccess(String endpoint);
}
