-- V10__Update_timestamps_to_timestamptz.sql
-- Convert existing TIMESTAMP columns to TIMESTAMPTZ for better timezone handling

-- Decks table
ALTER TABLE decks ALTER COLUMN last_studied_at TYPE TIMESTAMPTZ USING last_studied_at AT TIME ZONE 'UTC';
ALTER TABLE decks ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE decks ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- Flashcards table
ALTER TABLE flashcards ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE flashcards ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- Members table
ALTER TABLE members ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE members ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- Email verifications table
ALTER TABLE email_verifications ALTER COLUMN expires_at TYPE TIMESTAMPTZ USING expires_at AT TIME ZONE 'UTC';
