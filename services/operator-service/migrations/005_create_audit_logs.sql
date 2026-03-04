-- Migration: Create audit log tables
-- Date: 2026-02-06
-- Description: Creates tables for comprehensive audit logging (audit logs, API access logs, login logs)

-- 1. Operator Audit Log Table (for configuration changes)
CREATE TABLE IF NOT EXISTS operator_audit_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    action_description VARCHAR(500),
    performed_by VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    request_id VARCHAR(100),
    old_values TEXT, -- JSON string of old values
    new_values TEXT, -- JSON string of new values
    changed_fields VARCHAR(500), -- Comma-separated list of changed field names
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE SET NULL
);

-- Indexes for operator_audit_log
CREATE INDEX IF NOT EXISTS idx_audit_log_operator_id ON operator_audit_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action_type ON operator_audit_log(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON operator_audit_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_operator_action ON operator_audit_log(operator_id, action_type);

-- 2. Operator API Access Log Table (for all API calls)
CREATE TABLE IF NOT EXISTS operator_api_access_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT,
    endpoint VARCHAR(200) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    http_status INTEGER NOT NULL,
    request_ip VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    authenticated_by VARCHAR(100),
    request_id VARCHAR(100),
    response_time_ms BIGINT,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_access_log_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE SET NULL
);

-- Indexes for operator_api_access_log
CREATE INDEX IF NOT EXISTS idx_api_access_operator_id ON operator_api_access_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_api_access_endpoint ON operator_api_access_log(endpoint);
CREATE INDEX IF NOT EXISTS idx_api_access_created_at ON operator_api_access_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_api_access_status ON operator_api_access_log(http_status);
CREATE INDEX IF NOT EXISTS idx_api_access_operator_endpoint ON operator_api_access_log(operator_id, endpoint);

-- 3. Operator Login Log Table (for login attempts)
CREATE TABLE IF NOT EXISTS operator_login_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    login_status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, LOCKED
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    failure_reason VARCHAR(200),
    session_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for operator_login_log
CREATE INDEX IF NOT EXISTS idx_login_log_username ON operator_login_log(username);
CREATE INDEX IF NOT EXISTS idx_login_log_status ON operator_login_log(login_status);
CREATE INDEX IF NOT EXISTS idx_login_log_created_at ON operator_login_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_log_ip ON operator_login_log(ip_address);
CREATE INDEX IF NOT EXISTS idx_login_log_username_status ON operator_login_log(username, login_status);

-- Add comments for documentation
COMMENT ON TABLE operator_audit_log IS 'Audit log for operator configuration changes and administrative actions';
COMMENT ON TABLE operator_api_access_log IS 'Log of all API access to operator endpoints';
COMMENT ON TABLE operator_login_log IS 'Log of admin login attempts and sessions';

COMMENT ON COLUMN operator_audit_log.action_type IS 'Type of action: OPERATOR_CREATED, OPERATOR_UPDATED, MAINTENANCE_ENABLED, etc.';
COMMENT ON COLUMN operator_audit_log.old_values IS 'JSON string of values before change';
COMMENT ON COLUMN operator_audit_log.new_values IS 'JSON string of values after change';
COMMENT ON COLUMN operator_audit_log.changed_fields IS 'Comma-separated list of changed field names';

COMMENT ON COLUMN operator_api_access_log.endpoint IS 'API endpoint accessed (e.g., /api/v1/admin/operators/5/maintenance)';
COMMENT ON COLUMN operator_api_access_log.http_status IS 'HTTP response status code';
COMMENT ON COLUMN operator_api_access_log.response_time_ms IS 'Response time in milliseconds';

COMMENT ON COLUMN operator_login_log.login_status IS 'Login attempt status: SUCCESS, FAILED, LOCKED';
COMMENT ON COLUMN operator_login_log.failure_reason IS 'Reason for failed login (e.g., Invalid password)';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('operator_audit_log', 'operator_api_access_log', 'operator_login_log')
ORDER BY table_name, ordinal_position;
