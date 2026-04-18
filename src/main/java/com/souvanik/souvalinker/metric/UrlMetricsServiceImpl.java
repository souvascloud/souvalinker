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
@RequiredArgsConstructor
public class UrlMetricsServiceImpl implements UrlMetricsService {

    private final MeterRegistry meterRegistry;


    @Override
    public void incrementShortUrlCreated() {
        meterRegistry.counter("short_url_created_total").increment();
    }


    @Override
    public void incrementRedirectResolution(String source) {
        meterRegistry.counter("redirect_resolution_total", "source", source).increment();
    }



    @Override
    public void incrementRateLimitRejection(String endpoint) {
        meterRegistry.counter("rate_limit_rejections_total", "endpoint", endpoint).increment();
    }

    @Override
    public void recordRedirectLatency(long millis) {
        Timer.builder("redirect_resolution_latency").register(meterRegistry).record(millis, TimeUnit.MILLISECONDS);
    }
}