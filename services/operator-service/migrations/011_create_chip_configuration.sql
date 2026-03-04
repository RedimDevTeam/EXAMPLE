-- Migration: Create chip configuration tables
-- Date: 2026-02-12
-- Description: Creates tables for chip denominations and bet limit types

-- 1. Chip Denominations Table
-- Stores chip values with flexible count (e.g., 5 chips, 6 chips, 7 chips) per operator/game/currency
-- Chip index starts at 0 and can be any non-negative integer (e.g., 0-4 for 5 chips, 0-5 for 6 chips, 0-6 for 7 chips)
CREATE TABLE IF NOT EXISTS operator_chip_denominations (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    chip_index INTEGER NOT NULL CHECK (chip_index >= 0), -- Flexible: 0-based index (e.g., 0-4 for 5 chips, 0-5 for 6 chips, 0-6 for 7 chips)
    chip_value DECIMAL(19,2) NOT NULL CHECK (chip_value > 0), -- Value of this chip
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER, -- Order for UI display (0 = first chip shown)
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chip_denominations_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_chip_value_positive CHECK (chip_value > 0),
    UNIQUE(operator_id, game_id, currency_code, chip_index) -- One chip value per index per operator-game-currency
);

-- Indexes for operator_chip_denominations
CREATE INDEX IF NOT EXISTS idx_chip_denominations_operator_id ON operator_chip_denominations(operator_id);
CREATE INDEX IF NOT EXISTS idx_chip_denominations_game_id ON operator_chip_denominations(game_id);
CREATE INDEX IF NOT EXISTS idx_chip_denominations_currency ON operator_chip_denominations(currency_code);
CREATE INDEX IF NOT EXISTS idx_chip_denominations_active ON operator_chip_denominations(is_active);

-- 2. Bet Limit Types Table
-- Stores bet limit types (Standard, VIP, Promotional, Custom) per operator/game/currency
CREATE TABLE IF NOT EXISTS operator_bet_limit_types (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    limit_type VARCHAR(20) NOT NULL CHECK (limit_type IN ('STANDARD', 'VIP', 'PROMOTIONAL', 'CUSTOM')), -- Bet limit type
    min_bet_limit DECIMAL(19,2) NOT NULL CHECK (min_bet_limit > 0),
    max_bet_limit DECIMAL(19,2) NOT NULL CHECK (max_bet_limit >= min_bet_limit),
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bet_limit_types_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_bet_limit_type_min_max CHECK (min_bet_limit > 0 AND max_bet_limit >= min_bet_limit),
    UNIQUE(operator_id, game_id, currency_code, limit_type) -- One limit type per operator-game-currency combination
);

-- Indexes for operator_bet_limit_types
CREATE INDEX IF NOT EXISTS idx_bet_limit_types_operator_id ON operator_bet_limit_types(operator_id);
CREATE INDEX IF NOT EXISTS idx_bet_limit_types_game_id ON operator_bet_limit_types(game_id);
CREATE INDEX IF NOT EXISTS idx_bet_limit_types_currency ON operator_bet_limit_types(currency_code);
CREATE INDEX IF NOT EXISTS idx_bet_limit_types_type ON operator_bet_limit_types(limit_type);
CREATE INDEX IF NOT EXISTS idx_bet_limit_types_active ON operator_bet_limit_types(is_active);

-- Add comments for documentation
COMMENT ON TABLE operator_chip_denominations IS 'Chip denominations per operator/game/currency. Supports flexible chip counts (e.g., 5, 6, 7 chips) based on UI space availability.';
COMMENT ON COLUMN operator_chip_denominations.chip_index IS 'Chip index (0-based). Flexible: 0-4 for 5 chips, 0-5 for 6 chips, 0-6 for 7 chips, etc.';
COMMENT ON COLUMN operator_chip_denominations.chip_value IS 'Value of this chip denomination';
COMMENT ON COLUMN operator_chip_denominations.display_order IS 'Order for UI display (0 = first chip shown)';

COMMENT ON TABLE operator_bet_limit_types IS 'Bet limit types (Standard, VIP, Promotional, Custom) per operator/game/currency.';
COMMENT ON COLUMN operator_bet_limit_types.limit_type IS 'Bet limit type: STANDARD, VIP, PROMOTIONAL, or CUSTOM';
COMMENT ON COLUMN operator_bet_limit_types.min_bet_limit IS 'Minimum bet limit for this type';
COMMENT ON COLUMN operator_bet_limit_types.max_bet_limit IS 'Maximum bet limit for this type';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('operator_chip_denominations', 'operator_bet_limit_types')
ORDER BY table_name, ordinal_position;
