package com.souvanik.souvalinker.dto.response;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public record ApiResponse<T>(

        boolean success,

        String message,

        T data

) {}