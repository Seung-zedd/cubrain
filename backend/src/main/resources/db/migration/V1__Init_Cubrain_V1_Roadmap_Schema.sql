-- V1__Init_Schema.sql
-- Consolidated migration for MVP launch

-- 1. Extensions
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Tables

-- waitlist_users
CREATE TABLE waitlist_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_waitlist_users_email ON waitlist_users(email);

-- embeddings
CREATE TABLE embeddings (
    id UUID PRIMARY KEY,
    embedding vector(768),
    text TEXT,
    metadata JSONB
);
CREATE INDEX embedding_hnsw_idx ON embeddings USING hnsw (embedding vector_cosine_ops);

-- members
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    supabase_id VARCHAR(255) UNIQUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    daily_upload_count INTEGER NOT NULL DEFAULT 0,
    last_upload_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    modified_by VARCHAR(255)
);
CREATE INDEX idx_members_email ON members(email);

-- email_verifications
CREATE TABLE email_verifications (
    email VARCHAR(255) PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

-- decks
CREATE TABLE decks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    member_id BIGINT NOT NULL,
    study_progress INTEGER DEFAULT 0,
    last_studied_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_decks_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);
CREATE INDEX idx_decks_member_id ON decks(member_id);

-- flashcards
CREATE TABLE flashcards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcards_deck FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE
);
CREATE INDEX idx_flashcards_deck_id ON flashcards(deck_id);
