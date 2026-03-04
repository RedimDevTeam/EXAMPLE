-- Migration: Add custom currency support to existing operator_currencies table
-- Date: 2026-02-12
-- Description: Adds support for custom currencies (if table already exists)

-- Only run this if the table already exists (for existing deployments)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'operator_currencies'
    ) THEN
        -- Add is_custom column
        ALTER TABLE operator_currencies 
        ADD COLUMN IF NOT EXISTS is_custom BOOLEAN DEFAULT FALSE;
        
        -- Add currency_name column
        ALTER TABLE operator_currencies 
        ADD COLUMN IF NOT EXISTS currency_name VARCHAR(100);
        
        -- Update currency_code length from 3 to 10
        ALTER TABLE operator_currencies 
        ALTER COLUMN currency_code TYPE VARCHAR(10);
        
        -- Drop old constraints if they exist (safe to run even if they don't exist)
        ALTER TABLE operator_currencies 
        DROP CONSTRAINT IF EXISTS chk_currency_code_format;
        
        ALTER TABLE operator_currencies 
        DROP CONSTRAINT IF EXISTS chk_custom_currency_name;
        
        -- Add new constraint for flexible currency codes
        -- Note: We drop first, so this should always succeed
        BEGIN
            ALTER TABLE operator_currencies 
            ADD CONSTRAINT chk_currency_code_format CHECK (
                -- Standard ISO 4217: exactly 3 uppercase letters
                (is_custom = FALSE AND LENGTH(currency_code) = 3 AND currency_code = UPPER(currency_code)) OR
                -- Custom currency: 1-10 alphanumeric characters, uppercase
                (is_custom = TRUE AND LENGTH(currency_code) BETWEEN 1 AND 10 AND currency_code = UPPER(currency_code) AND currency_code ~ '^[A-Z0-9]+$')
            );
        EXCEPTION
            WHEN duplicate_object THEN
                RAISE NOTICE 'Constraint chk_currency_code_format already exists, skipping';
        END;
        
        -- Add constraint for custom currency name
        BEGIN
            ALTER TABLE operator_currencies 
            ADD CONSTRAINT chk_custom_currency_name CHECK (
                -- Custom currencies must have a name
                (is_custom = FALSE) OR (is_custom = TRUE AND currency_name IS NOT NULL AND LENGTH(TRIM(currency_name)) > 0)
            );
        EXCEPTION
            WHEN duplicate_object THEN
                RAISE NOTICE 'Constraint chk_custom_currency_name already exists, skipping';
        END;
        
        RAISE NOTICE 'Successfully added custom currency support';
    ELSE
        RAISE NOTICE 'Table operator_currencies does not exist - skipping migration';
    END IF;
END $$;
