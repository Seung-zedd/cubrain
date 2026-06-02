-- V6__Rename_Supabase_Id_To_Firebase_Uid.sql
ALTER TABLE members RENAME COLUMN supabase_id TO firebase_uid;
