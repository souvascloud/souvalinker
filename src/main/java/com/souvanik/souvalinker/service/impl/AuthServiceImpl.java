package com.souvanik.souvalinker.service.impl;

import com.souvanik.souvalinker.config.properties.AppProperties;
import com.souvanik.souvalinker.constants.SecurityConstants;
import com.souvanik.souvalinker.dto.payload.AuthPayload;
import com.souvanik.souvalinker.dto.request.LoginRequest;
import com.souvanik.souvalinker.dto.request.RegisterRequest;
import com.souvanik.souvalinker.entity.AuthToken;
import com.souvanik.souvalinker.entity.TokenType;
import com.souvanik.souvalinker.entity.User;
import com.souvanik.souvalinker.entity.UserStatus;
import com.souvanik.souvalinker.exception.BadRequestException;
import com.souvanik.souvalinker.exception.ResourceNotFoundException;
import com.souvanik.souvalinker.repository.AuthTokenRepository;
import com.souvanik.souvalinker.repository.UserRepository;
import com.souvanik.souvalinker.service.AuthService;
import com.souvanik.souvalinker.service.EmailService;
import com.souvanik.souvalinker.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;

    private final AuthTokenRepository authTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final EmailService emailService;

    private final AppProperties appProperties;



    @Override
    public void register( RegisterRequest request) {

        logger.info("Registering user email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            logger.warn("event=user_registration_rejected reason=email_exists email={}", request.email());
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByUsername(request.username())) {
            logger.warn("event=user_registration_rejected reason=username_exists username={}", request.username());
            throw new BadRequestException("Username already exists");
        }

        User user = new User();

        user.setUsername(request.username());

        user.setEmail(request.email());

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user.setStatus(UserStatus.INACTIVE);

        User savedUser = userRepository.save(user);

        String verificationToken = tokenService.generateVerificationToken();

        AuthToken authToken = authTokenRepository.findByUserIdAndType(
                savedUser.getId(),
                TokenType.VERIFY).orElse(
                        new AuthToken()
         );

        authToken.setUser(savedUser);

        authToken.setToken(verificationToken);

        authToken.setType(TokenType.VERIFY);

        authToken.setUsed(false);

        authToken.setExpiryTime(
                java.time.LocalDateTime
                        .now()
                        .plusHours(
                                appProperties.auth()
                                        .verifyTokenExpiryHours()
                        )
        );


        authTokenRepository.save(authToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        logger.info("event=user_registration_success userId={}", savedUser.getId());
    }



    @Override
    public AuthPayload login(LoginRequest request) {

        logger.info("event=login_attempt username={}", request.username());

        User user = userRepository.findByUsername(
                        request.username()
                )
                .orElseThrow(() -> {

                    logger.warn(
                            "event=login_rejected reason=user_not_found username={}",
                            request.username()
                    );

                    return new ResourceNotFoundException(
                            "User not found"
                    );
                });


        if (user.getStatus() != UserStatus.ACTIVE) {
            logger.warn("event=login_rejected reason=user_not_verified userId={}", user.getId());

            throw new BadRequestException("Email not verified");
        }


        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {

            logger.warn("event=login_rejected reason=invalid_credentials userId={}", user.getId());

            throw new BadCredentialsException("Invalid credentials");
        }


        String jwt = tokenService.generateJwt(user.getId());


        logger.info("event=login_success userId={}", user.getId());


        return new AuthPayload(jwt, SecurityConstants.TOKEN_TYPE_BEARER);
    }


    @Override
    public void verifyEmail(String token) {

        logger.info("event=email_verification_started");


        AuthToken authToken =
                authTokenRepository
                        .findByToken(token)
                        .orElseThrow(() -> {
                            logger.warn("event=email_verification_rejected reason=token_not_found");
                            return new ResourceNotFoundException("Invalid verification token");
                        });


        if (authToken.isExpired()) {
            logger.warn("event=email_verification_rejected reason=token_expired");
            throw new BadRequestException("Verification token expired");
        }



        if (authToken.isUsed()) {

            logger.warn("event=email_verification_rejected reason=token_used");

            throw new BadRequestException("Verification token already used");
        }



        if (authToken.getType() != TokenType.VERIFY) {
            logger.warn("event=email_verification_rejected reason=invalid_token_type");

            throw new BadRequestException("Invalid token type");
        }



        User user = authToken.getUser();

        user.setStatus(UserStatus.ACTIVE);

        authToken.setUsed(true);


        userRepository.save(user);

        authTokenRepository.save(authToken);


        logger.info("event=email_verification_success userId={}", user.getId());
    }


    @Override
    public void forgotPassword(String email) {

        logger.info("event=password_reset_requested email={}", email);


        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("event=password_reset_rejected reason=user_not_found email={}", email);
                    return new ResourceNotFoundException("User not found");
                });


        String resetToken = tokenService.generatePasswordResetToken();

        AuthToken authToken =
                authTokenRepository
                        .findByUserIdAndType(
                                user.getId(),
                                TokenType.RESET
                        )
                        .orElse(
                                new AuthToken()
                        );


        authToken.setUser(user);

        authToken.setToken(resetToken);

        authToken.setType(TokenType.RESET);

        authToken.setUsed(false);

        authToken.setExpiryTime(
                java.time.LocalDateTime
                        .now()
                        .plusMinutes(
                                appProperties.auth()
                                        .resetTokenExpiryMinutes()
                        )
        );


        authTokenRepository.save(authToken);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        logger.info("event=password_reset_email_sent userId={}", user.getId());
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        logger.info("event=password_reset_started");


        AuthToken authToken =
                authTokenRepository
                        .findByToken(token)
                        .orElseThrow(() -> {
                            logger.warn("event=password_reset_rejected reason=token_not_found");
                            return new ResourceNotFoundException("Invalid reset token");
                        });


        if (authToken.isExpired()) {
            logger.warn("event=password_reset_rejected reason=token_expired");
            throw new BadRequestException("Reset token expired");
        }



        if (authToken.isUsed()) {
            logger.warn("event=password_reset_rejected reason=token_used");

            throw new BadRequestException("Reset token already used");
        }


        if (authToken.getType() != TokenType.RESET) {
            logger.warn("event=password_reset_rejected reason=invalid_token_type");

            throw new BadRequestException("Invalid token type");
        }

        User user = authToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        authToken.setUsed(true);


        userRepository.save(user);

        authTokenRepository.save(authToken);

        logger.info("event=password_reset_success userId={}", user.getId());
    }

}