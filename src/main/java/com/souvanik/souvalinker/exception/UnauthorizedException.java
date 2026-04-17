package com.souvanik.souvalinker.exception;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public class UnauthorizedException  extends RuntimeException {

    public UnauthorizedException(
            String message) {

        super(message);
    }
}
