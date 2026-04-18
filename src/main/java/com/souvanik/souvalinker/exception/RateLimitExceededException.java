package com.souvanik.souvalinker.exception;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public class RateLimitExceededException
        extends RuntimeException {

    public RateLimitExceededException(String message) {

        super(message);
    }
}