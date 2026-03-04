-- Migration: Add maintenance mode fields to operators table
-- Date: 2026-02-06
-- Description: Adds maintenance mode fields for graceful maintenance handling

ALTER TABLE operators ADD COLUMN IF NOT EXISTS maintenance_mode BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE operators ADD COLUMN IF NOT EXISTS maintenance_start_time TIMESTAMP;
ALTER TABLE operators ADD COLUMN IF NOT EXISTS maintenance_end_time TIMESTAMP;
ALTER TABLE operators ADD COLUMN IF NOT EXISTS maintenance_message VARCHAR(1000);

-- Create index for performance (queries checking maintenance status)
CREATE INDEX IF NOT EXISTS idx_operators_maintenance_mode ON operators(maintenance_mode) WHERE maintenance_mode = TRUE;

-- Add comments for documentation
COMMENT ON COLUMN operators.maintenance_mode IS 'Whether operator is in maintenance mode';
COMMENT ON COLUMN operators.maintenance_start_time IS 'Scheduled start time for maintenance (null = immediate)';
COMMENT ON COLUMN operators.maintenance_end_time IS 'Scheduled end time for maintenance (null = indefinite)';
COMMENT ON COLUMN operators.maintenance_message IS 'Maintenance message displayed to players';

-- Verify columns were added
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'operators' 
  AND column_name IN ('maintenance_mode', 'maintenance_start_time', 'maintenance_end_time', 'maintenance_message')
ORDER BY ordinal_position;
