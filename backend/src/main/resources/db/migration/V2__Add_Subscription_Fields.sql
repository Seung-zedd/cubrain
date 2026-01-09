-- V2__Add_Subscription_Fields.sql
-- Add ends_at and customer_portal_url to members table

ALTER TABLE members
ADD COLUMN ends_at TIMESTAMPTZ,
ADD COLUMN customer_portal_url VARCHAR(255);
