-- Migration: Create bet limits tables
-- Date: 2026-02-12
-- Description: Creates tables for game-specific and operator-specific bet limits

-- 1. Game-Specific Bet Limits (applies to all operators)
CREATE TABLE IF NOT EXISTS game_bet_limits (
    id BIGSERIAL PRIMARY KEY,
    game_id VARCHAR(100) NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    min_bet DECIMAL(19,2) NOT NULL,
    max_bet DECIMAL(19,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP, -- NULL = currently active
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_min_max_bet CHECK (min_bet > 0 AND max_bet >= min_bet),
    CONSTRAINT chk_effective_dates CHECK (effective_to IS NULL OR effective_to > effective_from)
);

-- Indexes for game_bet_limits
CREATE INDEX IF NOT EXISTS idx_game_bet_limits_game_id ON game_bet_limits(game_id);
CREATE INDEX IF NOT EXISTS idx_game_bet_limits_provider_id ON game_bet_limits(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_game_bet_limits_currency ON game_bet_limits(currency_code);
CREATE INDEX IF NOT EXISTS idx_game_bet_limits_active ON game_bet_limits(is_active, effective_from, effective_to);

-- Unique index for active configs (one active config per game-currency combination)
CREATE UNIQUE INDEX IF NOT EXISTS idx_game_bet_limits_unique ON game_bet_limits(
    game_id, 
    currency_code, 
    effective_from
) WHERE is_active = TRUE;

-- 2. Game & Operator-Specific Bet Limits (overrides game-specific limits)
CREATE TABLE IF NOT EXISTS operator_game_bet_limits (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    min_bet DECIMAL(19,2) NOT NULL,
    max_bet DECIMAL(19,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP, -- NULL = currently active
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operator_game_bet_limits_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_operator_min_max_bet CHECK (min_bet > 0 AND max_bet >= min_bet),
    CONSTRAINT chk_operator_effective_dates CHECK (effective_to IS NULL OR effective_to > effective_from)
);

-- Indexes for operator_game_bet_limits
CREATE INDEX IF NOT EXISTS idx_operator_game_bet_limits_operator_id ON operator_game_bet_limits(operator_id);
CREATE INDEX IF NOT EXISTS idx_operator_game_bet_limits_game_id ON operator_game_bet_limits(game_id);
CREATE INDEX IF NOT EXISTS idx_operator_game_bet_limits_provider_id ON operator_game_bet_limits(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_operator_game_bet_limits_currency ON operator_game_bet_limits(currency_code);
CREATE INDEX IF NOT EXISTS idx_operator_game_bet_limits_active ON operator_game_bet_limits(is_active, effective_from, effective_to);

-- Unique index for active configs (one active config per operator-game-currency combination)
CREATE UNIQUE INDEX IF NOT EXISTS idx_operator_game_bet_limits_unique ON operator_game_bet_limits(
    operator_id, 
    game_id, 
    currency_code, 
    effective_from
) WHERE is_active = TRUE;

-- Add comments for documentation
COMMENT ON TABLE game_bet_limits IS 'Game-specific bet limits that apply to all operators for a game';
COMMENT ON TABLE operator_game_bet_limits IS 'Operator-specific bet limits that override game-specific limits';

COMMENT ON COLUMN game_bet_limits.game_id IS 'Game identifier (e.g., ROULETTE_EUROPEAN)';
COMMENT ON COLUMN game_bet_limits.game_provider_id IS 'Game provider identifier (e.g., PROVIDER_ROULETTE)';
COMMENT ON COLUMN game_bet_limits.currency_code IS 'Currency code (ISO 4217, e.g., USD, EUR)';
COMMENT ON COLUMN game_bet_limits.min_bet IS 'Minimum bet amount';
COMMENT ON COLUMN game_bet_limits.max_bet IS 'Maximum bet amount';
COMMENT ON COLUMN game_bet_limits.effective_to IS 'NULL = currently active, TIMESTAMP = expiration date';

COMMENT ON COLUMN operator_game_bet_limits.operator_id IS 'Operator ID (foreign key to operators table)';
COMMENT ON COLUMN operator_game_bet_limits.game_id IS 'Game identifier (e.g., ROULETTE_EUROPEAN)';
COMMENT ON COLUMN operator_game_bet_limits.game_provider_id IS 'Game provider identifier (e.g., PROVIDER_ROULETTE)';
COMMENT ON COLUMN operator_game_bet_limits.currency_code IS 'Currency code (ISO 4217, e.g., USD, EUR)';
COMMENT ON COLUMN operator_game_bet_limits.min_bet IS 'Minimum bet amount (overrides game-specific limit)';
COMMENT ON COLUMN operator_game_bet_limits.max_bet IS 'Maximum bet amount (overrides game-specific limit)';
COMMENT ON COLUMN operator_game_bet_limits.effective_to IS 'NULL = currently active, TIMESTAMP = expiration date';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('game_bet_limits', 'operator_game_bet_limits')
ORDER BY table_name, ordinal_position;
