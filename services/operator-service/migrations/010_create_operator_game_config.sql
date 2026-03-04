-- Migration: Create operator game configuration table
-- Date: 2026-02-12
-- Description: Creates table for enabling/disabling games per operator

-- Operator Game Configuration Table
-- Controls which games are available to which operators
CREATE TABLE IF NOT EXISTS operator_game_config (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL, -- Game provider identifier (e.g., PROVIDER_ROULETTE)
    game_id VARCHAR(100) NOT NULL, -- Game identifier (e.g., ROULETTE_EUROPEAN)
    is_enabled BOOLEAN DEFAULT TRUE, -- Whether this game is enabled for the operator
    is_active BOOLEAN DEFAULT TRUE, -- Whether this configuration is currently active
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- When this configuration becomes effective
    effective_to TIMESTAMP, -- When this configuration expires (NULL = currently active)
    launch_url VARCHAR(500), -- Custom game launch URL for this operator (optional)
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operator_game_config_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_effective_dates CHECK (effective_to IS NULL OR effective_to > effective_from),
    UNIQUE(operator_id, game_provider_id, game_id, effective_from) -- One config per operator-game-provider combination per effective date
);

-- Indexes for operator_game_config
CREATE INDEX IF NOT EXISTS idx_operator_game_config_operator_id ON operator_game_config(operator_id);
CREATE INDEX IF NOT EXISTS idx_operator_game_config_provider_id ON operator_game_config(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_operator_game_config_game_id ON operator_game_config(game_id);
CREATE INDEX IF NOT EXISTS idx_operator_game_config_enabled ON operator_game_config(is_enabled, is_active);
CREATE INDEX IF NOT EXISTS idx_operator_game_config_active ON operator_game_config(is_active, effective_from, effective_to);

-- Unique index for active configs (one active config per operator-provider-game combination)
CREATE UNIQUE INDEX IF NOT EXISTS idx_operator_game_config_unique ON operator_game_config(
    operator_id, 
    game_provider_id, 
    game_id
) WHERE is_active = TRUE;

-- Add comments for documentation
COMMENT ON TABLE operator_game_config IS 'Game configuration per operator. Controls which games are enabled/disabled for each operator.';
COMMENT ON COLUMN operator_game_config.operator_id IS 'Operator ID (foreign key to operators table)';
COMMENT ON COLUMN operator_game_config.game_provider_id IS 'Game provider identifier (e.g., PROVIDER_ROULETTE, PROVIDER_SLOTS)';
COMMENT ON COLUMN operator_game_config.game_id IS 'Game identifier (e.g., ROULETTE_EUROPEAN, SLOT_MAGIC_777)';
COMMENT ON COLUMN operator_game_config.is_enabled IS 'Whether this game is enabled for the operator';
COMMENT ON COLUMN operator_game_config.is_active IS 'Whether this configuration is currently active';
COMMENT ON COLUMN operator_game_config.effective_from IS 'When this configuration becomes effective';
COMMENT ON COLUMN operator_game_config.effective_to IS 'When this configuration expires (NULL = currently active)';
COMMENT ON COLUMN operator_game_config.launch_url IS 'Custom game launch URL for this operator (optional, overrides default)';

-- Verify table was created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'operator_game_config'
ORDER BY ordinal_position;
