package com.souvanik.souvalinker.controller;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */

import com.souvanik.souvalinker.annotation.RateLimited;
import com.souvanik.souvalinker.constants.MessageConstants;
import com.souvanik.souvalinker.dto.payload.AuthPayload;
import com.souvanik.souvalinker.dto.request.*;
import com.souvanik.souvalinker.dto.response.ApiResponse;
import com.souvanik.souvalinker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.souvanik.souvalinker.model.RateLimitType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    @RateLimited(strategy = REGISTER_IP)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>>
    register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.USER_REGISTERED, null));
    }


    @RateLimited(strategy = LOGIN_IP)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthPayload>> login(@Valid @RequestBody LoginRequest request) {

        AuthPayload payload = authService.login(request);

        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.LOGIN_SUCCESS, payload));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {

        authService.verifyEmail(token);

        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.EMAIL_VERIFIED, null));
    }

    @RateLimited(strategy = FORGOT_PASSWORD_IP)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {

        authService.forgotPassword(email);

        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.PASSWORD_RESET_EMAIL_SENT, null));
    }

    @RateLimited(strategy = RESET_PASSWORD_IP)
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.token() , request.newPassword());

        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.PASSWORD_RESET_SUCCESS, null));
    }

    @RateLimited(strategy = REFRESH_TOKEN)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthPayload>> refresh(@RequestBody RefreshRequest request) {

        AuthPayload payload = authService.refresh(request.refreshToken());

        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.TOKEN_REFRESH_SUCCESS, payload));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {

        authService.logout(request.refreshToken());

        return ResponseEntity.ok(new ApiResponse<>(true, MessageConstants.LOGOUT_SUCCESS, null));
    }
}
