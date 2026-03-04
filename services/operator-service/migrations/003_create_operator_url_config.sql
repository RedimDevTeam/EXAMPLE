-- Migration: Create operator_url_config table
-- Date: 2026-02-06
-- Description: Creates table for operator URL configuration (Request URL, Directory Path, Virtual Path)

CREATE TABLE IF NOT EXISTS operator_url_config (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL UNIQUE,
    request_url VARCHAR(500), -- Operator API base URL (e.g., https://operator.com/api)
    directory_path VARCHAR(200), -- Application directory mapping (e.g., /app/v1)
    virtual_path VARCHAR(200), -- Reverse proxy / routing path (e.g., /operator-api)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operator_url_config_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE
);

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_operator_url_config_operator_id ON operator_url_config(operator_id);

-- Add comments for documentation
COMMENT ON TABLE operator_url_config IS 'URL configuration for operators (API base URL, directory path, virtual path)';
COMMENT ON COLUMN operator_url_config.request_url IS 'Operator API base URL';
COMMENT ON COLUMN operator_url_config.directory_path IS 'Application directory mapping';
COMMENT ON COLUMN operator_url_config.virtual_path IS 'Reverse proxy / routing path';

-- Verify table was created
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'operator_url_config' 
ORDER BY ordinal_position;
