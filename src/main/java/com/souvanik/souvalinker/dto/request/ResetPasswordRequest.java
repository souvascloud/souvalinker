package com.souvanik.souvalinker.dto.request;

import jakarta.validation.constraints.NotBlank;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public record ResetPasswordRequest(

        @NotBlank
        String token,

        @NotBlank
        String newPassword

) {}