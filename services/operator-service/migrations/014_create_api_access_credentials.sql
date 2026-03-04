-- Migration: Create API access credentials table
-- Date: 2026-02-12
-- Description: Creates table for username/password authentication (in addition to API keys)

-- API Access Credentials Table
-- Stores username/password credentials for operator API access
CREATE TABLE IF NOT EXISTS operator_api_credentials (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE, -- Unique username for API access
    password_hash VARCHAR(255) NOT NULL, -- Hashed password (BCrypt)
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE, -- Account lockout after failed attempts
    failed_login_attempts INTEGER DEFAULT 0, -- Track failed login attempts
    locked_until TIMESTAMP, -- Lock expiration time
    last_login_at TIMESTAMP, -- Last successful login timestamp
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Password change timestamp
    expires_at TIMESTAMP, -- Optional credential expiration
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_credentials_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_username_length CHECK (LENGTH(TRIM(username)) >= 3),
    CONSTRAINT chk_failed_attempts CHECK (failed_login_attempts >= 0)
);

-- Indexes for operator_api_credentials
CREATE INDEX IF NOT EXISTS idx_api_credentials_operator_id ON operator_api_credentials(operator_id);
CREATE INDEX IF NOT EXISTS idx_api_credentials_username ON operator_api_credentials(username);
CREATE INDEX IF NOT EXISTS idx_api_credentials_active ON operator_api_credentials(is_active);
CREATE INDEX IF NOT EXISTS idx_api_credentials_locked ON operator_api_credentials(is_locked);

-- Add comments for documentation
COMMENT ON TABLE operator_api_credentials IS 'API access credentials (username/password) for operators. Provides alternative authentication method to API keys.';
COMMENT ON COLUMN operator_api_credentials.username IS 'Unique username for API authentication (minimum 3 characters)';
COMMENT ON COLUMN operator_api_credentials.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN operator_api_credentials.is_locked IS 'Account lockout status (locked after failed login attempts)';
COMMENT ON COLUMN operator_api_credentials.failed_login_attempts IS 'Number of consecutive failed login attempts';
COMMENT ON COLUMN operator_api_credentials.locked_until IS 'Lock expiration timestamp (null if not locked)';
COMMENT ON COLUMN operator_api_credentials.expires_at IS 'Optional credential expiration timestamp';

-- Verify table was created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'operator_api_credentials'
ORDER BY ordinal_position;
