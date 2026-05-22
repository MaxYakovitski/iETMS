-- create refresh_tokens table
CREATE TABLE refresh_tokens (
                                id          BIGSERIAL PRIMARY KEY,
                                user_id     BIGINT NOT NULL REFERENCES users(id),
                                token_hash  VARCHAR(64) NOT NULL UNIQUE,
                                expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
                                revoked     BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- drop token_version column from users
ALTER TABLE users DROP COLUMN token_version;