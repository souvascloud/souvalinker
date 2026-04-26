package com.souvanik.souvalinker.metric;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.TimeUnit;


/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */

@Service
public class UrlMetricsServiceImpl implements UrlMetricsService {


    private final MeterRegistry meterRegistry;

    private final Timer redirectLatencyTimer;

    public UrlMetricsServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.redirectLatencyTimer = Timer.builder("app_redirect_resolution_latency")
                .description("Time taken to resolve short URL")
                .register(meterRegistry);
    }

    @Override
    public void incrementShortUrlCreated() {
        meterRegistry.counter("app_short_url_created_total").increment();
    }

    @Override
    public void incrementRedirectResolution(String source) {
        meterRegistry.counter("app_redirect_resolution_total", "source", source).increment();
    }

    @Override
    public void incrementRateLimitRejection(String endpoint) {
        meterRegistry.counter("app_rate_limit_rejections_total", "endpoint", endpoint).increment();
    }

    @Override
    public void incrementRateLimitSuccess(String endpoint) {
        meterRegistry.counter("app_rate_limit_success_total", "endpoint", endpoint).increment();
    }

    @Override
    public void recordRedirectLatency(long millis) {
        redirectLatencyTimer.record(millis, TimeUnit.MILLISECONDS);
    }
}