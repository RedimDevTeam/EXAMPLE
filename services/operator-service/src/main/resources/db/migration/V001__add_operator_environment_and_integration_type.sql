-- Migration: Add environment and integration_type columns to operators table
-- Date: 2026-02-06
-- Description: Adds operator environment (LIVE, UAT, STAGING, DEMO) and integration type (SHARED_WALLET, FUND_TRANSFER, AMS)

-- Add environment column
ALTER TABLE operators ADD COLUMN IF NOT EXISTS environment VARCHAR(20);

-- Add integration_type column
ALTER TABLE operators ADD COLUMN IF NOT EXISTS integration_type VARCHAR(20);

-- Add comments for documentation
COMMENT ON COLUMN operators.environment IS 'Operator environment: LIVE, UAT, STAGING, or DEMO';
COMMENT ON COLUMN operators.integration_type IS 'Integration type: SHARED_WALLET, FUND_TRANSFER, or AMS';

-- Optional: Update existing operators with default values if needed
-- UPDATE operators SET environment = 'LIVE' WHERE environment IS NULL;
-- UPDATE operators SET integration_type = 'SHARED_WALLET' WHERE integration_type IS NULL;
