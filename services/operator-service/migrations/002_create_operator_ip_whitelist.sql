-- Migration: Create operator_ip_whitelist table
-- Date: 2026-02-06
-- Description: Creates table for operator IP whitelist configuration

CREATE TABLE IF NOT EXISTS operator_ip_whitelist (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL, -- Supports IPv6 (max 45 chars)
    allowed_endpoints TEXT[], -- Array of endpoint patterns (e.g., ['/api/v1/bets', '/api/v1/wallet/debit'])
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operator_ip_whitelist_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT uk_operator_ip_whitelist UNIQUE (operator_id, ip_address)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_operator_ip_whitelist_operator_id ON operator_ip_whitelist(operator_id);
CREATE INDEX IF NOT EXISTS idx_operator_ip_whitelist_ip_address ON operator_ip_whitelist(ip_address);
CREATE INDEX IF NOT EXISTS idx_operator_ip_whitelist_active ON operator_ip_whitelist(operator_id, is_active);

-- Add comments for documentation
COMMENT ON TABLE operator_ip_whitelist IS 'IP whitelist entries for operators';
COMMENT ON COLUMN operator_ip_whitelist.ip_address IS 'IP address (IPv4, IPv6, or localhost)';
COMMENT ON COLUMN operator_ip_whitelist.allowed_endpoints IS 'Array of allowed endpoint patterns. NULL = all endpoints allowed';
COMMENT ON COLUMN operator_ip_whitelist.is_active IS 'Whether this whitelist entry is active';

-- Verify table was created
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'operator_ip_whitelist' 
ORDER BY ordinal_position;
