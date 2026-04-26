--- =========================================
-- EXTENSIONS (for case-insensitive support if needed)
-- =========================================
-- Optional: enables better text handling
CREATE EXTENSION IF NOT EXISTS citext;

-- =========================================
-- USERS
-- =========================================

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(255) NOT NULL,

                       password_hash VARCHAR(255) NOT NULL,

                       status VARCHAR(20) NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Case-insensitive uniqueness
CREATE UNIQUE INDEX uk_users_email_ci ON users (LOWER(email));
CREATE UNIQUE INDEX uk_users_username_ci ON users (LOWER(username));

-- =========================================
-- TRIGGER: AUTO UPDATE updated_at
-- =========================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =========================================
-- URL MAPPING
-- =========================================

CREATE TABLE url_mapping (
                             id BIGSERIAL PRIMARY KEY,

                             long_url TEXT NOT NULL,
                             short_code VARCHAR(10) NOT NULL,

                             user_id BIGINT,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_url_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE SET NULL
);

-- Unique short code
CREATE UNIQUE INDEX uk_short_code ON url_mapping(short_code);

-- Performance indexes
CREATE INDEX idx_url_user_id ON url_mapping(user_id);
CREATE INDEX idx_url_user_created ON url_mapping(user_id, created_at);

-- =========================================
-- AUTH TOKEN
-- =========================================

CREATE TABLE auth_token (
                            id BIGSERIAL PRIMARY KEY,

                            user_id BIGINT NOT NULL,

    -- Store HASH instead of raw token (security)
                            token_hash VARCHAR(255) NOT NULL,

                            type VARCHAR(20) NOT NULL,

                            expiry_time TIMESTAMP NOT NULL,

                            is_used BOOLEAN NOT NULL DEFAULT FALSE,

                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_token_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE
);

-- Unique token hash
CREATE UNIQUE INDEX uk_token_hash ON auth_token(token_hash);

-- Performance indexes
CREATE INDEX idx_token_user_id ON auth_token(user_id);
CREATE INDEX idx_token_expiry ON auth_token(expiry_time);

