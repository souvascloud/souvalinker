package com.souvanik.souvalinker.controller;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */

import com.souvanik.souvalinker.constants.MessageConstants;
import com.souvanik.souvalinker.dto.payload.AuthPayload;
import com.souvanik.souvalinker.dto.request.LoginRequest;
import com.souvanik.souvalinker.dto.request.RegisterRequest;
import com.souvanik.souvalinker.dto.response.ApiResponse;
import com.souvanik.souvalinker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;



    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>>
    register(@Valid
            @RequestBody
             RegisterRequest request) {

        authService.register(
                request
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        MessageConstants.USER_REGISTERED,
                        null
                )
        );
    }



    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthPayload>>
    login(
            @Valid
            @RequestBody
            LoginRequest request) {

        AuthPayload payload =
                authService.login(
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        MessageConstants.LOGIN_SUCCESS,
                        payload
                )
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>>
    verifyEmail(
            @RequestParam
            String token) {

        authService.verifyEmail(
                token
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        MessageConstants.EMAIL_VERIFIED,
                        null
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>>
    forgotPassword(
            @RequestParam
            String email) {

        authService.forgotPassword(
                email
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        MessageConstants
                                .PASSWORD_RESET_EMAIL_SENT,
                        null
                )
        );
    }
}
