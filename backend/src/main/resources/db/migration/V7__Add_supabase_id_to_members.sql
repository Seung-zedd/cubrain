-- Migration to add supabase_id column to members table
ALTER TABLE members ADD COLUMN supabase_id VARCHAR(255) UNIQUE;
