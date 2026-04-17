package com.souvanik.souvalinker.repository;

import com.souvanik.souvalinker.entity.AuthToken;
import com.souvanik.souvalinker.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * Copyright (c) 2026 Souvanik Saha
 *
 * Licensed under the MIT License.
 * https://opensource.org/licenses/MIT
 */
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByToken(String token);

    Optional<AuthToken> findByUserIdAndType(
            Long userId,
            TokenType type
    );
}
