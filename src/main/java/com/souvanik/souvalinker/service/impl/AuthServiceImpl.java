package com.souvanik.souvalinker.service.impl;

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



    @Override
    public void register( RegisterRequest request) {

        logger.info("Registering user email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByUsername(request.username())) {
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
                                SecurityConstants
                                        .VERIFY_TOKEN_EXPIRY_HOURS
                        )
        );


        authTokenRepository.save(authToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        logger.info("User registered successfully email={}", savedUser.getEmail());
    }



    @Override
    public AuthPayload login(
            LoginRequest request) {

        User user =
                userRepository
                        .findByUsername(
                                request.username()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );


        if (user.getStatus()
                != UserStatus.ACTIVE) {

            throw new RuntimeException(
                    "Email not verified"
            );
        }


        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new RuntimeException(
                    "Invalid credentials"
            );
        }


        String jwt =
                tokenService.generateJwt(
                        user.getId()
                );


        return new AuthPayload(
                jwt,
                SecurityConstants
                        .TOKEN_TYPE_BEARER
        );
    }



    @Override
    public void verifyEmail(
            String token) {

        logger.info(
                "Starting email verification for token {}",
                token
        );


        AuthToken authToken =
                authTokenRepository
                        .findByToken(token)
                        .orElseThrow(() -> {

                            logger.error(
                                    "Verification token not found"
                            );

                            return new ResourceNotFoundException(
                                    "Invalid verification token"
                            );
                        });



        if (authToken.isExpired()) {

            logger.error(
                    "Verification token expired"
            );

            throw new BadRequestException(
                    "Verification token expired"
            );
        }



        if (authToken.isUsed()) {

            logger.error(
                    "Verification token already used"
            );

            throw new BadRequestException(
                    "Verification token already used"
            );
        }



        if (authToken.getType()
                != TokenType.VERIFY) {

            logger.error(
                    "Invalid token type"
            );

            throw new BadRequestException(
                    "Invalid token type"
            );
        }



        User user =
                authToken.getUser();


        user.setStatus(
                UserStatus.ACTIVE
        );


        authToken.setUsed(
                true
        );


        userRepository.save(
                user
        );

        authTokenRepository.save(
                authToken
        );


        logger.info(
                "User verified successfully: {}",
                user.getEmail()
        );
    }


    @Override
    public void forgotPassword(String email) {
        logger.info("Processing forgot password email={}", email);

        User user = userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );


        String resetToken = tokenService.generatePasswordResetToken();


        AuthToken authToken = authTokenRepository.findByUserIdAndType(
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
                                SecurityConstants
                                        .RESET_TOKEN_EXPIRY_MINUTES
                        )
        );


        authTokenRepository.save(authToken);


        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);


        logger.info("Reset email sent successfully email={}", user.getEmail());
    }

    @Override
    public void resetPassword(String token, String newPassword) {

        logger.info("Starting password reset");

        AuthToken authToken =
                authTokenRepository
                        .findByToken(token)
                        .orElseThrow(() -> {

                            logger.error(
                                    "Reset token not found"
                            );

                            return new ResourceNotFoundException(
                                    "Invalid reset token"
                            );
                        });



        if (authToken.isExpired()) {

            logger.error(
                    "Reset token expired"
            );

            throw new BadRequestException(
                    "Reset token expired"
            );
        }



        if (authToken.isUsed()) {

            logger.error(
                    "Reset token already used"
            );

            throw new BadRequestException(
                    "Reset token already used"
            );
        }



        if (authToken.getType()
                != TokenType.RESET) {

            logger.error(
                    "Invalid token type"
            );

            throw new BadRequestException(
                    "Invalid token type"
            );
        }



        User user =
                authToken.getUser();


        user.setPasswordHash(
                passwordEncoder.encode(
                        newPassword
                )
        );


        authToken.setUsed(
                true
        );


        userRepository.save(
                user
        );

        authTokenRepository.save(
                authToken
        );


        logger.info(
                "Password reset successful for {}",
                user.getEmail()
        );
    }

}