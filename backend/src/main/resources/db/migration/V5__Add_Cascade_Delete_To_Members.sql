-- V5__Add_Cascade_Delete_To_Members.sql
-- Note: This migration was intended to add a foreign key to auth.users (Supabase Auth).
-- However, since the database is hosted on Railway and does not contain the 'auth' schema,
-- a physical foreign key constraint cannot be established.
-- Deletion of member data is instead handled via application logic in the SvelteKit server action
-- and a corresponding backend delete endpoint.

-- We keep this file to satisfy Flyway, but it performs no actions that would fail.
SELECT 1;
