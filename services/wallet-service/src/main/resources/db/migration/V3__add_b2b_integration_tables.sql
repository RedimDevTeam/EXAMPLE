-- Migration: Add B2B Integration Support
-- Version: V3
-- Date: 2026-02-06
-- Description: Adds extended fields to wallet_transactions and creates tables for B2B Integration
--              (Fund Transfer two-step flow and Operational APIs)

-- ============================================================================
-- PART 1: Add Extended Fields to wallet_transactions Table
-- ============================================================================
-- Industry Standard Naming: game_id (not game_key), transaction_subtype_id (not txn_sub_type_id),
--                          brand_id (not skin_id), language (not lang)

-- Add player_level (Bet limit tier: 0=Low, 1=Regular, 2=High, 3=VIP)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'player_level'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN player_level INTEGER;
    END IF;
END $$;

-- Add unit_type (CENTS or DECIMAL)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'unit_type'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN unit_type VARCHAR(10);
    END IF;
END $$;

-- Add round_id (Game round reference identifier)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'round_id'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN round_id VARCHAR(100);
    END IF;
END $$;

-- Add game_id (Game identifier - Industry standard naming)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'game_id'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN game_id VARCHAR(100);
    END IF;
END $$;

-- Add hand_id (Game hand identifier for card games)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'hand_id'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN hand_id VARCHAR(100);
    END IF;
END $$;

-- Add transaction_subtype_id (Transaction subtype code: 300-307 - Industry standard naming)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'transaction_subtype_id'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN transaction_subtype_id INTEGER;
    END IF;
END $$;

-- Add brand_id (Brand/skin identifier - Industry standard naming)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'brand_id'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN brand_id VARCHAR(100);
    END IF;
END $$;

-- Add agent_id (Agent system reference)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'agent_id'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN agent_id VARCHAR(100);
    END IF;
END $$;

-- Add language (ISO 639-1 language code - Industry standard naming)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'language'
    ) THEN
        ALTER TABLE wallet_transactions ADD COLUMN language VARCHAR(10);
    END IF;
END $$;

-- ============================================================================
-- PART 2: Create Indexes for Extended Fields
-- ============================================================================

-- Index for round_id (for game round queries)
CREATE INDEX IF NOT EXISTS idx_txn_round_id ON wallet_transactions(round_id) 
WHERE round_id IS NOT NULL;

-- Index for game_id (for game-specific queries)
CREATE INDEX IF NOT EXISTS idx_txn_game_id ON wallet_transactions(game_id) 
WHERE game_id IS NOT NULL;

-- Index for transaction_subtype_id (for transaction type queries)
CREATE INDEX IF NOT EXISTS idx_txn_subtype_id ON wallet_transactions(transaction_subtype_id) 
WHERE transaction_subtype_id IS NOT NULL;

-- Composite index for operator, player, and round (common query pattern)
CREATE INDEX IF NOT EXISTS idx_txn_operator_player_round ON wallet_transactions(operator_id, player_id, round_id) 
WHERE round_id IS NOT NULL;

-- ============================================================================
-- PART 3: Create pending_fund_transactions Table (Fund Transfer Two-Step Flow)
-- ============================================================================

CREATE TABLE IF NOT EXISTS pending_fund_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(100) UNIQUE NOT NULL,
    operator_id BIGINT NOT NULL,
    player_id VARCHAR(100) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    unit_type VARCHAR(10) NOT NULL CHECK (unit_type IN ('CENTS', 'DECIMAL')),
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('deposit', 'withdrawal')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'EXPIRED', 'CANCELLED')),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    confirmed_by VARCHAR(100),
    operator_response JSONB,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Indexes for pending_fund_transactions
CREATE INDEX IF NOT EXISTS idx_pending_operator_player ON pending_fund_transactions(operator_id, player_id);
CREATE INDEX IF NOT EXISTS idx_pending_status ON pending_fund_transactions(status);
CREATE INDEX IF NOT EXISTS idx_pending_expires ON pending_fund_transactions(expires_at) 
WHERE expires_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_pending_payment_id ON pending_fund_transactions(payment_id);
CREATE INDEX IF NOT EXISTS idx_pending_created_at ON pending_fund_transactions(created_at);

-- Add comment to table
COMMENT ON TABLE pending_fund_transactions IS 'Stores pending fund transfer transactions for two-step flow (Request → Confirm)';

-- ============================================================================
-- PART 4: Create player_account_status Table (Operational APIs)
-- ============================================================================

CREATE TABLE IF NOT EXISTS player_account_status (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    player_id VARCHAR(100) NOT NULL,
    is_blocked BOOLEAN DEFAULT FALSE NOT NULL,
    blocked_reason VARCHAR(500),
    blocked_at TIMESTAMP,
    blocked_by VARCHAR(100),
    unblocked_at TIMESTAMP,
    unblocked_by VARCHAR(100),
    kicked_out_at TIMESTAMP,
    kicked_out_by VARCHAR(100),
    last_activity_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_account_status_operator_player UNIQUE (operator_id, player_id)
);

-- Indexes for player_account_status
CREATE INDEX IF NOT EXISTS idx_account_status ON player_account_status(operator_id, player_id, is_blocked);
CREATE INDEX IF NOT EXISTS idx_account_blocked ON player_account_status(is_blocked) 
WHERE is_blocked = TRUE;
CREATE INDEX IF NOT EXISTS idx_account_last_activity ON player_account_status(last_activity_at) 
WHERE last_activity_at IS NOT NULL;

-- Add comment to table
COMMENT ON TABLE player_account_status IS 'Tracks player account status for operational APIs (block/unblock/kickout)';

-- ============================================================================
-- PART 5: Add Constraints and Validation
-- ============================================================================

-- Add CHECK constraint for unit_type in wallet_transactions (if column exists)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'unit_type'
    ) THEN
        -- Drop existing constraint if any
        ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_unit_type;
        -- Add new constraint
        ALTER TABLE wallet_transactions ADD CONSTRAINT chk_unit_type 
            CHECK (unit_type IS NULL OR unit_type IN ('CENTS', 'DECIMAL'));
    END IF;
END $$;

-- Add CHECK constraint for player_level in wallet_transactions (if column exists)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'player_level'
    ) THEN
        -- Drop existing constraint if any
        ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_player_level;
        -- Add new constraint (0=Low, 1=Regular, 2=High, 3=VIP)
        ALTER TABLE wallet_transactions ADD CONSTRAINT chk_player_level 
            CHECK (player_level IS NULL OR (player_level >= 0 AND player_level <= 3));
    END IF;
END $$;

-- Add CHECK constraint for transaction_subtype_id in wallet_transactions (if column exists)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'wallet_transactions' AND column_name = 'transaction_subtype_id'
    ) THEN
        -- Drop existing constraint if any
        ALTER TABLE wallet_transactions DROP CONSTRAINT IF EXISTS chk_transaction_subtype_id;
        -- Add new constraint (300-307 are standard codes)
        ALTER TABLE wallet_transactions ADD CONSTRAINT chk_transaction_subtype_id 
            CHECK (transaction_subtype_id IS NULL OR (transaction_subtype_id >= 300 AND transaction_subtype_id <= 307));
    END IF;
END $$;

-- ============================================================================
-- Migration Complete
-- ============================================================================
-- Summary:
-- 1. Added 9 extended fields to wallet_transactions table
-- 2. Created 4 indexes for extended fields
-- 3. Created pending_fund_transactions table for Fund Transfer two-step flow
-- 4. Created player_account_status table for Operational APIs
-- 5. Added validation constraints
--
-- Next Steps:
-- - Update WalletTransaction entity to include new fields
-- - Create B2B Integration DTOs with industry-standard naming
-- - Implement B2B Integration services
