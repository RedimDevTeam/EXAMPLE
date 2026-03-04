-- Migration: Create commission configuration tables
-- Date: 2026-02-06
-- Description: Creates tables for commission models (GGR-Based, Fixed Price per Bet, Winnings-Based)

-- Note: Using VARCHAR with CHECK constraint instead of PostgreSQL enum for better Hibernate compatibility

-- 2. Commission Configuration Table
CREATE TABLE IF NOT EXISTS operator_commission_config (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    game_id VARCHAR(100), -- NULL = all games from provider, specific game_id = applies only to that game
    commission_model VARCHAR(50) NOT NULL CHECK (commission_model IN ('GGR_BASED', 'FIXED_PRICE_PER_BET', 'WINNINGS_BASED')),
    
    -- GGR-Based Commission Fields
    operator_ggr_rate DECIMAL(5,2), -- Percentage (0.00-100.00)
    provider_ggr_rate DECIMAL(5,2), -- Percentage (0.00-100.00)
    
    -- Fixed Price per Bet Fields
    fixed_price_per_bet DECIMAL(19,2), -- Fixed amount per bet
    fixed_price_currency VARCHAR(3), -- Currency for fixed price
    
    -- Winnings-Based Commission Fields
    winnings_commission_rate DECIMAL(5,2), -- Percentage of winnings (0.00-100.00)
    operator_winnings_share DECIMAL(5,2), -- Percentage of commission (0.00-100.00)
    provider_winnings_share DECIMAL(5,2), -- Percentage of commission (0.00-100.00)
    
    -- Common Fields
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP, -- NULL = currently active, TIMESTAMP = expired
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_commission_config_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_ggr_rates_sum CHECK (
        commission_model != 'GGR_BASED' OR 
        (operator_ggr_rate IS NOT NULL AND provider_ggr_rate IS NOT NULL AND 
         ABS((operator_ggr_rate + provider_ggr_rate) - 100.00) < 0.01)
    ),
    CONSTRAINT chk_fixed_price CHECK (
        commission_model != 'FIXED_PRICE_PER_BET' OR 
        (fixed_price_per_bet IS NOT NULL AND fixed_price_per_bet > 0)
    ),
    CONSTRAINT chk_winnings_shares_sum CHECK (
        commission_model != 'WINNINGS_BASED' OR 
        (winnings_commission_rate IS NOT NULL AND operator_winnings_share IS NOT NULL AND 
         provider_winnings_share IS NOT NULL AND
         ABS((operator_winnings_share + provider_winnings_share) - 100.00) < 0.01)
    ),
    CONSTRAINT chk_effective_dates CHECK (
        effective_to IS NULL OR effective_to > effective_from
    )
);

-- Indexes for operator_commission_config
CREATE INDEX IF NOT EXISTS idx_commission_config_operator_id ON operator_commission_config(operator_id);
CREATE INDEX IF NOT EXISTS idx_commission_config_provider_id ON operator_commission_config(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_commission_config_game_id ON operator_commission_config(game_id);
CREATE INDEX IF NOT EXISTS idx_commission_config_active ON operator_commission_config(is_active, effective_from, effective_to);

-- Unique index for active configs (one active config per operator-provider-game combination)
CREATE UNIQUE INDEX IF NOT EXISTS idx_commission_config_unique ON operator_commission_config(
    operator_id, 
    game_provider_id, 
    COALESCE(game_id, 'ALL'),
    effective_from
) WHERE is_active = TRUE;

-- 3. Commission Calculation History Table
CREATE TABLE IF NOT EXISTS operator_commission_calculations (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    game_id VARCHAR(100),
    commission_config_id BIGINT,
    commission_model VARCHAR(50) NOT NULL CHECK (commission_model IN ('GGR_BASED', 'FIXED_PRICE_PER_BET', 'WINNINGS_BASED')),
    
    -- Calculation Period
    calculation_period_start TIMESTAMP NOT NULL,
    calculation_period_end TIMESTAMP NOT NULL,
    
    -- GGR-Based Metrics
    total_bets DECIMAL(19,2),
    total_winnings DECIMAL(19,2),
    ggr DECIMAL(19,2), -- Total Bets - Total Winnings
    operator_commission DECIMAL(19,2),
    provider_commission DECIMAL(19,2),
    
    -- Fixed Price Metrics
    number_of_bets INTEGER,
    fixed_price_per_bet DECIMAL(19,2),
    total_operator_fee DECIMAL(19,2),
    
    -- Winnings-Based Metrics
    total_winnings_amount DECIMAL(19,2),
    winnings_commission_rate DECIMAL(5,2),
    total_commission DECIMAL(19,2),
    operator_commission_share DECIMAL(19,2),
    provider_commission_share DECIMAL(19,2),
    
    -- Common Fields
    currency VARCHAR(3) NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    calculated_by VARCHAR(100), -- System or admin username
    settlement_cycle_id BIGINT, -- Reference to settlement cycle if applicable
    
    -- Foreign Keys
    CONSTRAINT fk_commission_calc_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT fk_commission_calc_config FOREIGN KEY (commission_config_id) REFERENCES operator_commission_config(id) ON DELETE SET NULL
);

-- Indexes for operator_commission_calculations
CREATE INDEX IF NOT EXISTS idx_commission_calc_operator_id ON operator_commission_calculations(operator_id);
CREATE INDEX IF NOT EXISTS idx_commission_calc_provider_id ON operator_commission_calculations(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_commission_calc_period ON operator_commission_calculations(calculation_period_start, calculation_period_end);
CREATE INDEX IF NOT EXISTS idx_commission_calc_settlement ON operator_commission_calculations(settlement_cycle_id);

-- Add comments for documentation
COMMENT ON TABLE operator_commission_config IS 'Commission configuration per operator-game provider combination';
COMMENT ON TABLE operator_commission_calculations IS 'Commission calculation history for audit and reporting';

COMMENT ON COLUMN operator_commission_config.game_id IS 'NULL = applies to all games from provider, specific game_id = applies only to that game';
COMMENT ON COLUMN operator_commission_config.operator_ggr_rate IS 'Operator GGR commission rate (%)';
COMMENT ON COLUMN operator_commission_config.provider_ggr_rate IS 'Gaming Provider GGR commission rate (%)';
COMMENT ON COLUMN operator_commission_config.fixed_price_per_bet IS 'Fixed price per bet (for FIXED_PRICE_PER_BET model)';
COMMENT ON COLUMN operator_commission_config.winnings_commission_rate IS 'Commission rate on winnings (%)';
COMMENT ON COLUMN operator_commission_config.operator_winnings_share IS 'Operator share of commission (%)';
COMMENT ON COLUMN operator_commission_config.provider_winnings_share IS 'Gaming Provider share of commission (%)';
COMMENT ON COLUMN operator_commission_config.created_by IS 'Gaming Provider Global Admin username';

COMMENT ON COLUMN operator_commission_calculations.ggr IS 'Gross Gaming Revenue (Total Bets - Total Winnings)';
COMMENT ON COLUMN operator_commission_calculations.settlement_cycle_id IS 'Reference to settlement cycle if applicable';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('operator_commission_config', 'operator_commission_calculations')
ORDER BY table_name, ordinal_position;
