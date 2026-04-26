package com.souvanik.souvalinker.event;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public record PasswordChangedEvent(
        String email
) {}