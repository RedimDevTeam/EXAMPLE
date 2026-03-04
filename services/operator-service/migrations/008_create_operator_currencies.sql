-- Migration: Create operator currencies table
-- Date: 2026-02-12
-- Description: Creates table for multi-currency support per operator

-- Operator Currencies Table
-- Allows operators to support multiple currencies (not just baseCurrency)
-- Supports both ISO 4217 standard currencies (USD, EUR, GBP) and custom currencies
CREATE TABLE IF NOT EXISTS operator_currencies (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    currency_code VARCHAR(10) NOT NULL, -- ISO 4217 currency code (USD, EUR, GBP) or custom currency code (up to 10 chars)
    is_custom BOOLEAN DEFAULT FALSE, -- TRUE for custom currencies, FALSE for ISO 4217 standard currencies
    currency_name VARCHAR(100), -- Display name for custom currencies (e.g., "Operator Tokens", "Loyalty Points")
    is_default BOOLEAN DEFAULT FALSE, -- One currency per operator should be default
    is_active BOOLEAN DEFAULT TRUE,
    exchange_rate DECIMAL(19,6), -- Exchange rate relative to base currency (optional, for future use)
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operator_currencies_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_currency_code_format CHECK (
        -- Standard ISO 4217: exactly 3 uppercase letters
        (is_custom = FALSE AND LENGTH(currency_code) = 3 AND currency_code = UPPER(currency_code)) OR
        -- Custom currency: 1-10 alphanumeric characters, uppercase
        (is_custom = TRUE AND LENGTH(currency_code) BETWEEN 1 AND 10 AND currency_code = UPPER(currency_code) AND currency_code ~ '^[A-Z0-9]+$')
    ),
    CONSTRAINT chk_custom_currency_name CHECK (
        -- Custom currencies must have a name
        (is_custom = FALSE) OR (is_custom = TRUE AND currency_name IS NOT NULL AND LENGTH(TRIM(currency_name)) > 0)
    ),
    CONSTRAINT chk_exchange_rate CHECK (exchange_rate IS NULL OR exchange_rate > 0),
    UNIQUE(operator_id, currency_code) -- One currency code per operator
);

-- Indexes for operator_currencies
CREATE INDEX IF NOT EXISTS idx_operator_currencies_operator_id ON operator_currencies(operator_id);
CREATE INDEX IF NOT EXISTS idx_operator_currencies_currency_code ON operator_currencies(currency_code);
CREATE INDEX IF NOT EXISTS idx_operator_currencies_active ON operator_currencies(is_active);
CREATE INDEX IF NOT EXISTS idx_operator_currencies_default ON operator_currencies(operator_id, is_default) WHERE is_default = TRUE;

-- Add comments for documentation
COMMENT ON TABLE operator_currencies IS 'Multi-currency support for operators. Supports both ISO 4217 standard currencies (USD, EUR, GBP) and custom currencies (TOKEN, POINTS, CREDITS, etc.).';
COMMENT ON COLUMN operator_currencies.operator_id IS 'Operator ID (foreign key to operators table)';
COMMENT ON COLUMN operator_currencies.currency_code IS 'ISO 4217 currency code (e.g., USD, EUR, GBP) or custom currency code (e.g., TOKEN, POINTS, CREDITS, up to 10 characters)';
COMMENT ON COLUMN operator_currencies.is_custom IS 'TRUE for custom currencies, FALSE for ISO 4217 standard currencies';
COMMENT ON COLUMN operator_currencies.currency_name IS 'Display name for custom currencies (required for custom currencies, e.g., "Operator Tokens", "Loyalty Points")';
COMMENT ON COLUMN operator_currencies.is_default IS 'TRUE if this is the default currency for the operator (one per operator)';
COMMENT ON COLUMN operator_currencies.is_active IS 'Whether this currency is currently active for the operator';
COMMENT ON COLUMN operator_currencies.exchange_rate IS 'Exchange rate relative to base currency (optional, for future use)';

-- Verify table was created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'operator_currencies'
ORDER BY ordinal_position;
