-- =========================================
-- USERS
-- =========================================

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       username VARCHAR(50) NOT NULL UNIQUE,

                       email VARCHAR(255) NOT NULL UNIQUE,

                       password_hash VARCHAR(255) NOT NULL,

                       status VARCHAR(20) NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);



-- =========================================
-- URL MAPPING
-- =========================================

CREATE TABLE url_mapping (
                             id BIGSERIAL PRIMARY KEY,

                             long_url TEXT NOT NULL,

                             short_code VARCHAR(10) NOT NULL UNIQUE,

                             user_id BIGINT,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_url_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE SET NULL
);

-- Keep this index
CREATE INDEX idx_user_id
    ON url_mapping(user_id);



-- =========================================
-- AUTH TOKEN
-- =========================================

CREATE TABLE auth_token (
                            id BIGSERIAL PRIMARY KEY,

                            user_id BIGINT NOT NULL,

                            token VARCHAR(255) NOT NULL UNIQUE,

                            type VARCHAR(20) NOT NULL,

                            expiry_time TIMESTAMP NOT NULL,

                            is_used BOOLEAN NOT NULL DEFAULT FALSE,

                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_token_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE
);

-- Keep this index
CREATE INDEX idx_token_user_id
    ON auth_token(user_id);