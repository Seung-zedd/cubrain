-- V9__Add_study_progress_to_decks.sql
ALTER TABLE decks ADD COLUMN study_progress INTEGER DEFAULT 0;
