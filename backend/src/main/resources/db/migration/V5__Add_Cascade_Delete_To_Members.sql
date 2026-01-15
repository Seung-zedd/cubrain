-- V5__Add_Cascade_Delete_To_Members.sql
-- Ensure that deleting a user from auth.users also deletes their record in the members table

ALTER TABLE members
DROP CONSTRAINT IF EXISTS members_supabase_id_fkey;

-- Note: We assume supabase_id is compatible with auth.users.id (UUID)
-- If it's VARCHAR, we might need to cast it, but we follow the requested logic.
ALTER TABLE members
ADD CONSTRAINT members_supabase_id_fkey
FOREIGN KEY (supabase_id)
REFERENCES auth.users (id)
ON DELETE CASCADE;
