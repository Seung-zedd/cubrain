-- Initial migration: Create users table
-- Note: Follows the naming strategy defined in the application (snake_case)

CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    kakao_id BIGINT NOT NULL UNIQUE,
    nickname VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(500),
    email VARCHAR(255),

    -- Audit fields from BaseEntity
    created_by VARCHAR(255),
    modified_by VARCHAR(255),

    -- Timestamp fields from BaseTimeEntity
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups by kakao_id
CREATE INDEX idx_users_kakao_id ON users(kakao_id);

-- Index for email searches (if needed)
CREATE INDEX idx_users_email ON users(email);
