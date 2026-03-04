-- Migration: Remove redundant columns
-- These are now logged or can be retrieved from other tables for better normalization

-- Remove request_payload column (logged instead)
ALTER TABLE wallet_transactions DROP COLUMN IF EXISTS request_payload;

-- Remove response_payload column (logged instead)
ALTER TABLE wallet_transactions DROP COLUMN IF EXISTS response_payload;

-- Remove operator_url column (can be retrieved from operator_wallet_config table)
ALTER TABLE wallet_transactions DROP COLUMN IF EXISTS operator_url;

-- Note: error_message is kept (small, useful for queries)
