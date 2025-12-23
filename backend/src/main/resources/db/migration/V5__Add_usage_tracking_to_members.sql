-- Migration to add usage tracking columns to members table
ALTER TABLE members ADD COLUMN daily_upload_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE members ADD COLUMN last_upload_date DATE NOT NULL DEFAULT CURRENT_DATE;
