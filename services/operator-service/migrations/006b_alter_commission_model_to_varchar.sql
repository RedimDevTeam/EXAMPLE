-- Migration: Alter commission_model column from enum to VARCHAR
-- Date: 2026-02-12
-- Description: Changes commission_model column from PostgreSQL enum to VARCHAR with CHECK constraint
--              for better Hibernate compatibility

-- Only run this if the enum type was already created
-- This script handles the conversion from enum to VARCHAR

DO $$ 
BEGIN
    -- Check if commission_model_type enum exists and tables exist
    IF EXISTS (
        SELECT 1 FROM pg_type WHERE typname = 'commission_model_type'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'operator_commission_config'
    ) THEN
        -- Alter operator_commission_config table using temporary column approach
        -- Step 1: Add temporary VARCHAR column
        ALTER TABLE operator_commission_config 
        ADD COLUMN commission_model_temp VARCHAR(50);
        
        -- Step 2: Copy data from enum column to temp column
        -- Use string concatenation to force enum to text conversion
        UPDATE operator_commission_config 
        SET commission_model_temp = (commission_model || '')::VARCHAR(50);
        
        -- Step 3: Drop the old enum column
        ALTER TABLE operator_commission_config 
        DROP COLUMN commission_model;
        
        -- Step 4: Rename temp column to original name
        ALTER TABLE operator_commission_config 
        RENAME COLUMN commission_model_temp TO commission_model;
        
        -- Step 5: Make it NOT NULL if it was before
        ALTER TABLE operator_commission_config 
        ALTER COLUMN commission_model SET NOT NULL;
        
        -- Step 6: Add CHECK constraint (drop if exists first)
        ALTER TABLE operator_commission_config 
        DROP CONSTRAINT IF EXISTS chk_commission_model;
        
        ALTER TABLE operator_commission_config 
        ADD CONSTRAINT chk_commission_model 
        CHECK (commission_model IN ('GGR_BASED', 'FIXED_PRICE_PER_BET', 'WINNINGS_BASED'));
        
        -- Alter operator_commission_calculations table if it exists
        IF EXISTS (
            SELECT 1 FROM information_schema.tables WHERE table_name = 'operator_commission_calculations'
        ) THEN
            -- Step 1: Add temporary VARCHAR column
            ALTER TABLE operator_commission_calculations 
            ADD COLUMN commission_model_temp VARCHAR(50);
            
            -- Step 2: Copy data from enum column to temp column
            -- Use string concatenation to force enum to text conversion
            UPDATE operator_commission_calculations 
            SET commission_model_temp = (commission_model || '')::VARCHAR(50);
            
            -- Step 3: Drop the old enum column
            ALTER TABLE operator_commission_calculations 
            DROP COLUMN commission_model;
            
            -- Step 4: Rename temp column to original name
            ALTER TABLE operator_commission_calculations 
            RENAME COLUMN commission_model_temp TO commission_model;
            
            -- Step 5: Make it NOT NULL if it was before
            ALTER TABLE operator_commission_calculations 
            ALTER COLUMN commission_model SET NOT NULL;
            
            ALTER TABLE operator_commission_calculations 
            DROP CONSTRAINT IF EXISTS chk_commission_model_calc;
            
            ALTER TABLE operator_commission_calculations 
            ADD CONSTRAINT chk_commission_model_calc 
            CHECK (commission_model IN ('GGR_BASED', 'FIXED_PRICE_PER_BET', 'WINNINGS_BASED'));
        END IF;
        
        -- Drop the enum type (optional - comment out if you want to keep it)
        -- DROP TYPE commission_model_type;
        
        RAISE NOTICE 'Successfully converted commission_model from enum to VARCHAR';
    ELSE
        RAISE NOTICE 'Enum type or tables do not exist - skipping conversion';
    END IF;
END $$;
