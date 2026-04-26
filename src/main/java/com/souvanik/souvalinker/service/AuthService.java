package com.souvanik.souvalinker.service;

import com.souvanik.souvalinker.dto.payload.AuthPayload;
import com.souvanik.souvalinker.dto.request.LoginRequest;
import com.souvanik.souvalinker.dto.request.RegisterRequest;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface AuthService {

    void register(RegisterRequest request);

    void verifyEmail(String token);

    AuthPayload login(LoginRequest request);

    AuthPayload refresh(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
