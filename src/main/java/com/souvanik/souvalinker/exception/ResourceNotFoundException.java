package com.souvanik.souvalinker.exception;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(
            String message) {

        super(message);
    }
}
