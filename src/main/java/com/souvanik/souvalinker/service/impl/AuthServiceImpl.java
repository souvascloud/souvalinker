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
import com.souvanik.souvalinker.event.UserRegisteredEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

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
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties appProperties;


    @Override
    @Transactional
    public void register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();
        String username = request.username().trim().toLowerCase();

        logger.info("event=register_attempt email={}", email);

        Optional<User> existingUser = userRepository.findByEmailOrUsername(email, username);

        if (existingUser.isPresent()) {
            User existing = existingUser.get();

            if (existing.getEmail().equals(email)) {
                throw new BadRequestException("Email already exists");
            }
            if (existing.getUsername().equals(username)) {
                throw new BadRequestException("Username already exists");
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.INACTIVE);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Email or Username already exists");
        }

        String rawToken = tokenService.generateVerificationToken();
        String tokenHash = tokenService.hashToken(rawToken);

        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setToken(tokenHash);
        authToken.setType(TokenType.VERIFY);
        authToken.setUsed(false);
        authToken.setExpiryTime(LocalDateTime.now(ZoneOffset.UTC).plusHours(appProperties.auth().verifyTokenExpiryHours()));

        authTokenRepository.save(authToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(user.getEmail(), rawToken));

        logger.info("event=register_success userId={}", user.getId());
    }


    @Override
    public AuthPayload login(LoginRequest request) {

        String username = request.username().trim().toLowerCase();
        String usernameHash = Integer.toHexString(username.hashCode());

        logger.info("event=login_attempt usernameHash={}", usernameHash);

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            logger.warn("event=login_failed usernameHash={}", usernameHash);
            throw new BadCredentialsException("Invalid username or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Email not verified");
        }

        String accessToken = tokenService.generateAccessToken(user.getId());
        String refreshToken = createAndStoreRefreshToken(user);

        logger.info("event=login_success userId={}", user.getId());

        return new AuthPayload(accessToken, refreshToken, SecurityConstants.TOKEN_TYPE_BEARER);
    }



    @Override
    @Transactional
    public AuthPayload refresh(String refreshToken) {

        String tokenHash = tokenService.hashToken(refreshToken);

        AuthToken existingToken = authTokenRepository.findByToken(tokenHash).orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (existingToken.getType() != TokenType.REFRESH) {
            throw new BadRequestException("Invalid token type");
        }

        if (existingToken.isUsed()) {
            throw new BadRequestException("Refresh token already used");
        }

        if (existingToken.isExpired()) {
            throw new BadRequestException("Refresh token expired");
        }

        User user = existingToken.getUser();


        existingToken.setUsed(true);

        String newAccessToken = tokenService.generateAccessToken(user.getId());
        String newRefreshToken = createAndStoreRefreshToken(user);

        return new AuthPayload(newAccessToken, newRefreshToken, SecurityConstants.TOKEN_TYPE_BEARER);
    }


    @Override
    @Transactional
    public void logout(String refreshToken) {

        String tokenHash = tokenService.hashToken(refreshToken);

        AuthToken token = authTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (token.getType() != TokenType.REFRESH) {
            throw new BadRequestException("Invalid token type");
        }

        if (token.isUsed()) {
            return;
        }

        token.setUsed(true);

        logger.info("event=logout_success userId={}", token.getUser().getId());
    }


    @Override
    @Transactional
    public void verifyEmail(String token) {

        String tokenHash = tokenService.hashToken(token);

        AuthToken authToken = authTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (authToken.getType() != TokenType.VERIFY) {
            throw new BadRequestException("Invalid token type");
        }

        if (authToken.isUsed()) {
            throw new BadRequestException("Token already used");
        }

        if (authToken.isExpired()) {
            throw new BadRequestException("Token expired");
        }

        User user = authToken.getUser();
        user.setStatus(UserStatus.ACTIVE);

        authToken.setUsed(true);

        logger.info("event=email_verified userId={}", user.getId());
    }



    @Override
    public void forgotPassword(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            String token = tokenService.generatePasswordResetToken();

            AuthToken authToken = new AuthToken();
            authToken.setUser(user);
            authToken.setToken(token);
            authToken.setType(TokenType.RESET);
            authToken.setUsed(false);
            authToken.setExpiryTime(
                    LocalDateTime.now().plusMinutes(
                            appProperties.auth().resetTokenExpiryMinutes()
                    )
            );

            authTokenRepository.save(authToken);

            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });

        logger.info("event=forgot_password_requested email={}", email);
    }


    @Override
    public void resetPassword(String token, String newPassword) {

        AuthToken authToken = authTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (authToken.isExpired() || authToken.isUsed()) {
            throw new BadRequestException("Token invalid or expired");
        }

        if (authToken.getType() != TokenType.RESET) {
            throw new BadRequestException("Invalid token type");
        }

        User user = authToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        authToken.setUsed(true);

        userRepository.save(user);
        authTokenRepository.save(authToken);

        logger.info("event=password_reset_success userId={}", user.getId());
    }

    private String createAndStoreRefreshToken(User user) {

        String rawToken = tokenService.generateRefreshToken();
        String tokenHash = tokenService.hashToken(rawToken);

        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setToken(tokenHash);
        token.setType(TokenType.REFRESH);
        token.setUsed(false);
        token.setExpiryTime(LocalDateTime.now(ZoneOffset.UTC).plusDays(appProperties.auth().refreshTokenExpiryDays()));
        authTokenRepository.save(token);

        return rawToken;
    }
}