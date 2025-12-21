-- Migration to create members and email_verifications tables
-- These were missing from the initial setup

CREATE TABLE IF NOT EXISTS members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS email_verifications (
    email VARCHAR(255) PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Index for faster email lookups on members table
CREATE INDEX IF NOT EXISTS idx_members_email ON members(email);
