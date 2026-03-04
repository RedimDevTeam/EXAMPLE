-- Migration: Make chip_index flexible (remove 0-6 constraint)
-- Date: 2026-02-12
-- Description: Updates chip_index constraint to support flexible chip counts (5, 6, 7, etc.)

-- Drop the existing CHECK constraint on chip_index (if it exists)
DO $$ 
BEGIN
    -- Drop the constraint if it exists
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'operator_chip_denominations_chip_index_check'
        AND table_name = 'operator_chip_denominations'
    ) THEN
        ALTER TABLE operator_chip_denominations 
        DROP CONSTRAINT operator_chip_denominations_chip_index_check;
    END IF;
    
    -- Add new flexible constraint (chip_index >= 0)
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'operator_chip_denominations_chip_index_check'
        AND table_name = 'operator_chip_denominations'
    ) THEN
        ALTER TABLE operator_chip_denominations 
        ADD CONSTRAINT operator_chip_denominations_chip_index_check 
        CHECK (chip_index >= 0);
    END IF;
EXCEPTION
    WHEN duplicate_object THEN
        -- Constraint already exists, skip
        NULL;
END $$;

-- Update comments
COMMENT ON TABLE operator_chip_denominations IS 'Chip denominations per operator/game/currency. Supports flexible chip counts (e.g., 5, 6, 7 chips) based on UI space availability.';
COMMENT ON COLUMN operator_chip_denominations.chip_index IS 'Chip index (0-based). Flexible: 0-4 for 5 chips, 0-5 for 6 chips, 0-6 for 7 chips, etc.';

-- Verify constraint was updated
SELECT 
    constraint_name, 
    check_clause 
FROM information_schema.check_constraints 
WHERE constraint_name = 'operator_chip_denominations_chip_index_check';
