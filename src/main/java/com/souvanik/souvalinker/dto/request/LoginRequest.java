package com.souvanik.souvalinker.dto.request;

import jakarta.validation.constraints.NotBlank;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public record LoginRequest(

        @NotBlank
        String username,

        @NotBlank
        String password

) {}