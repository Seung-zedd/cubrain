-- Initial migration: Create waitlist_users table
-- This table stores email addresses of users who joined the waitlist

CREATE TABLE waitlist_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    
    -- Timestamp fields from BaseTimeEntity
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster email lookups
CREATE INDEX idx_waitlist_users_email ON waitlist_users(email);
