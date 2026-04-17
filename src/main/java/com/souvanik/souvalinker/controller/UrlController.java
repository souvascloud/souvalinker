package com.souvanik.souvalinker.controller;

import com.souvanik.souvalinker.constants.MessageConstants;
import com.souvanik.souvalinker.dto.payload.ShortUrlPayload;
import com.souvanik.souvalinker.dto.request.CreateShortUrlRequest;
import com.souvanik.souvalinker.dto.response.ApiResponse;
import com.souvanik.souvalinker.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/urls")
public class UrlController {

    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);

    private final UrlService urlService;


    @PostMapping
    public ResponseEntity<ApiResponse<ShortUrlPayload>> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request, Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        logger.info("Creating short URL for userId={}", userId);

        ShortUrlPayload payload = urlService.createShortUrl(request, userId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        MessageConstants.URL_CREATED,
                        payload
                )
        );
    }


    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {

        logger.info("Redirect requested shortCode={}", shortCode);

        String originalUrl = urlService.resolveOriginalUrl(shortCode);

        return ResponseEntity
                .status(302)
                .location(
                        URI.create(
                                originalUrl
                        )
                )
                .build();
    }
}
