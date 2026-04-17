package com.souvanik.souvalinker.service;

import com.souvanik.souvalinker.dto.payload.ShortUrlPayload;
import com.souvanik.souvalinker.dto.request.CreateShortUrlRequest;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface UrlService {
    ShortUrlPayload createShortUrl(CreateShortUrlRequest request, Long userId);

    String resolveOriginalUrl(String shortCode);
}
