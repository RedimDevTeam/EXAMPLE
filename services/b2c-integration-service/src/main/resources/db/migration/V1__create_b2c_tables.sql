-- Migration: Create B2C Integration Tables
-- Version: 1
-- Description: Creates tables for B2C Provider Wallet Model integration

-- Provider Configuration Table
CREATE TABLE IF NOT EXISTS provider_config (
    id BIGSERIAL PRIMARY KEY,
    provider_id VARCHAR(100) UNIQUE NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    api_base_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    api_secret VARCHAR(255), -- For HMAC signature
    auth_type VARCHAR(50) NOT NULL DEFAULT 'API_KEY', -- API_KEY, HMAC, OAUTH
    supports_xml BOOLEAN DEFAULT FALSE,
    supports_json BOOLEAN DEFAULT TRUE,
    timeout_ms INT DEFAULT 5000,
    retry_attempts INT DEFAULT 3,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_auth_type CHECK (auth_type IN ('API_KEY', 'HMAC', 'OAUTH'))
);

-- Indexes for provider_config
CREATE INDEX IF NOT EXISTS idx_provider_active ON provider_config(is_active);
CREATE INDEX IF NOT EXISTS idx_provider_id ON provider_config(provider_id);

-- Provider Transactions Table
CREATE TABLE IF NOT EXISTS provider_transactions (
    id BIGSERIAL PRIMARY KEY,
    provider_id VARCHAR(100) NOT NULL,
    player_id VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    transaction_subtype_id INTEGER NOT NULL, -- Industry standard naming (300-307)
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    unit_type VARCHAR(10) NOT NULL, -- CENTS or DECIMAL
    player_level INTEGER,
    game_id VARCHAR(100), -- Industry standard naming
    round_id VARCHAR(100),
    hand_id VARCHAR(100),
    brand_id VARCHAR(100),
    agent_id VARCHAR(100),
    language VARCHAR(10),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED
    provider_response JSONB,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    UNIQUE(provider_id, transaction_id),
    CONSTRAINT chk_unit_type CHECK (unit_type IN ('CENTS', 'DECIMAL')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_transaction_subtype CHECK (transaction_subtype_id BETWEEN 300 AND 307)
);

-- Indexes for provider_transactions
CREATE INDEX IF NOT EXISTS idx_provider_player ON provider_transactions(provider_id, player_id);
CREATE INDEX IF NOT EXISTS idx_provider_status ON provider_transactions(provider_id, status);
CREATE INDEX IF NOT EXISTS idx_provider_txn_id ON provider_transactions(provider_id, transaction_id);
CREATE INDEX IF NOT EXISTS idx_provider_round_id ON provider_transactions(round_id);
CREATE INDEX IF NOT EXISTS idx_provider_game_id ON provider_transactions(game_id);

-- Comments
COMMENT ON TABLE provider_config IS 'B2C Provider configuration - stores provider API endpoints and credentials';
COMMENT ON TABLE provider_transactions IS 'B2C Provider transactions - tracks all wallet operations with providers';
COMMENT ON COLUMN provider_config.auth_type IS 'Authentication type: API_KEY, HMAC, or OAUTH';
COMMENT ON COLUMN provider_config.supports_xml IS 'Whether provider supports legacy XML envelope format';
COMMENT ON COLUMN provider_transactions.transaction_subtype_id IS 'Industry standard transaction subtype code (300-307)';
COMMENT ON COLUMN provider_transactions.unit_type IS 'Amount unit type: CENTS or DECIMAL';
