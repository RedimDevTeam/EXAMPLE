-- Migration: Create reports access management tables
-- Date: 2026-02-12
-- Description: Creates tables for hierarchical role-based report access control

-- Report Role Enum (stored as VARCHAR with CHECK constraint)
-- Hierarchical roles: CASINO_ADMIN, GROUP_ADMIN, GLOBAL_ADMIN

-- Reports Access Management Table
-- Stores role-based access control for reports per operator/user
CREATE TABLE IF NOT EXISTS operator_report_access (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    user_identifier VARCHAR(100) NOT NULL, -- Username or user ID
    report_role VARCHAR(20) NOT NULL CHECK (report_role IN ('CASINO_ADMIN', 'GROUP_ADMIN', 'GLOBAL_ADMIN')), -- Hierarchical role
    allowed_report_types TEXT[], -- Array of report type names (e.g., ['BET_REPORT', 'SETTLEMENT_REPORT'])
    allowed_operators BIGINT[], -- Array of operator IDs this user can access reports for (null = all operators)
    can_view_all_operators BOOLEAN DEFAULT FALSE, -- Global admin can view all operators
    can_export_reports BOOLEAN DEFAULT TRUE, -- Permission to export reports
    can_schedule_reports BOOLEAN DEFAULT FALSE, -- Permission to schedule automated reports
    access_level VARCHAR(20) DEFAULT 'READ_ONLY' CHECK (access_level IN ('READ_ONLY', 'READ_WRITE', 'FULL_ACCESS')), -- Access level
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP, -- Optional access expiration
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_access_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    UNIQUE(operator_id, user_identifier) -- One access record per operator-user combination
);

-- Report Access Log Table
-- Tracks report access for audit purposes
CREATE TABLE IF NOT EXISTS operator_report_access_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    user_identifier VARCHAR(100) NOT NULL,
    report_type VARCHAR(100) NOT NULL, -- Type of report accessed
    report_role VARCHAR(20) NOT NULL,
    access_method VARCHAR(20) NOT NULL CHECK (access_method IN ('VIEW', 'EXPORT', 'SCHEDULE', 'DOWNLOAD')), -- How report was accessed
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45), -- IP address of the request
    user_agent VARCHAR(500), -- User agent string
    CONSTRAINT fk_report_access_log_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE
);

-- Indexes for operator_report_access
CREATE INDEX IF NOT EXISTS idx_report_access_operator_id ON operator_report_access(operator_id);
CREATE INDEX IF NOT EXISTS idx_report_access_user_id ON operator_report_access(user_identifier);
CREATE INDEX IF NOT EXISTS idx_report_access_role ON operator_report_access(report_role);
CREATE INDEX IF NOT EXISTS idx_report_access_active ON operator_report_access(is_active);

-- Indexes for operator_report_access_log
CREATE INDEX IF NOT EXISTS idx_report_access_log_operator_id ON operator_report_access_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_report_access_log_user_id ON operator_report_access_log(user_identifier);
CREATE INDEX IF NOT EXISTS idx_report_access_log_accessed_at ON operator_report_access_log(accessed_at);
CREATE INDEX IF NOT EXISTS idx_report_access_log_report_type ON operator_report_access_log(report_type);

-- Add comments for documentation
COMMENT ON TABLE operator_report_access IS 'Role-based access control for reports. Hierarchical roles: CASINO_ADMIN (lowest), GROUP_ADMIN (middle), GLOBAL_ADMIN (highest).';
COMMENT ON COLUMN operator_report_access.report_role IS 'Hierarchical role: CASINO_ADMIN, GROUP_ADMIN, or GLOBAL_ADMIN';
COMMENT ON COLUMN operator_report_access.allowed_report_types IS 'Array of report types this user can access (null = all types)';
COMMENT ON COLUMN operator_report_access.allowed_operators IS 'Array of operator IDs this user can access (null = all operators for GLOBAL_ADMIN)';
COMMENT ON COLUMN operator_report_access.can_view_all_operators IS 'GLOBAL_ADMIN can view reports for all operators';
COMMENT ON COLUMN operator_report_access.access_level IS 'Access level: READ_ONLY, READ_WRITE, or FULL_ACCESS';

COMMENT ON TABLE operator_report_access_log IS 'Audit log for report access. Tracks who accessed which reports and when.';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('operator_report_access', 'operator_report_access_log')
ORDER BY table_name, ordinal_position;
