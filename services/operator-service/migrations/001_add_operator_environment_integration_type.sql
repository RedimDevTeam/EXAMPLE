-- Manual Migration Script: Add environment and integration_type to operators table
-- Run this script manually on your database
-- Date: 2026-02-06

-- Add environment column
ALTER TABLE operators ADD COLUMN IF NOT EXISTS environment VARCHAR(20);

-- Add integration_type column  
ALTER TABLE operators ADD COLUMN IF NOT EXISTS integration_type VARCHAR(20);

-- Add comments for documentation
COMMENT ON COLUMN operators.environment IS 'Operator environment: LIVE, UAT, STAGING, or DEMO';
COMMENT ON COLUMN operators.integration_type IS 'Integration type: SHARED_WALLET, FUND_TRANSFER, or AMS';

-- Verify columns were added
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'operators' 
  AND column_name IN ('environment', 'integration_type');
