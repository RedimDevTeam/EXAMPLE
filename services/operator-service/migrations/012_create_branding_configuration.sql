-- Migration: Create branding configuration tables
-- Date: 2026-02-12
-- Description: Creates tables for operator logos, game provider logos, and game logos

-- 1. Operator Branding Table
-- Stores operator logos (PNG, JPG, SVG)
CREATE TABLE IF NOT EXISTS operator_branding (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    logo_type VARCHAR(20) NOT NULL CHECK (logo_type IN ('OPERATOR_LOGO', 'FAVICON', 'MOBILE_LOGO', 'DESKTOP_LOGO')), -- Type of logo
    file_name VARCHAR(255) NOT NULL, -- Original filename
    file_path VARCHAR(500) NOT NULL, -- Storage path/URL
    file_format VARCHAR(10) NOT NULL CHECK (file_format IN ('PNG', 'JPG', 'JPEG', 'SVG')), -- File format
    file_size BIGINT, -- File size in bytes
    width INTEGER, -- Image width in pixels (for PNG/JPG)
    height INTEGER, -- Image height in pixels (for PNG/JPG)
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0, -- Order for multiple logos
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branding_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    UNIQUE(operator_id, logo_type, display_order) -- One logo per type per display order
);

-- Indexes for operator_branding
CREATE INDEX IF NOT EXISTS idx_branding_operator_id ON operator_branding(operator_id);
CREATE INDEX IF NOT EXISTS idx_branding_logo_type ON operator_branding(logo_type);
CREATE INDEX IF NOT EXISTS idx_branding_active ON operator_branding(is_active);

-- 2. Game Provider Branding Table
-- Stores game provider logos per operator
CREATE TABLE IF NOT EXISTS operator_game_provider_branding (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    logo_type VARCHAR(20) NOT NULL CHECK (logo_type IN ('PROVIDER_LOGO', 'ICON', 'BANNER')), -- Type of logo
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_format VARCHAR(10) NOT NULL CHECK (file_format IN ('PNG', 'JPG', 'JPEG', 'SVG')),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_provider_branding_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    UNIQUE(operator_id, game_provider_id, logo_type, display_order)
);

-- Indexes for operator_game_provider_branding
CREATE INDEX IF NOT EXISTS idx_provider_branding_operator_id ON operator_game_provider_branding(operator_id);
CREATE INDEX IF NOT EXISTS idx_provider_branding_provider_id ON operator_game_provider_branding(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_branding_active ON operator_game_provider_branding(is_active);

-- 3. Game Branding Table
-- Stores game logos per operator
CREATE TABLE IF NOT EXISTS operator_game_branding (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    game_provider_id VARCHAR(100) NOT NULL,
    logo_type VARCHAR(20) NOT NULL CHECK (logo_type IN ('GAME_LOGO', 'THUMBNAIL', 'BANNER', 'ICON')), -- Type of logo
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_format VARCHAR(10) NOT NULL CHECK (file_format IN ('PNG', 'JPG', 'JPEG', 'SVG')),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_game_branding_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    UNIQUE(operator_id, game_id, logo_type, display_order)
);

-- Indexes for operator_game_branding
CREATE INDEX IF NOT EXISTS idx_game_branding_operator_id ON operator_game_branding(operator_id);
CREATE INDEX IF NOT EXISTS idx_game_branding_game_id ON operator_game_branding(game_id);
CREATE INDEX IF NOT EXISTS idx_game_branding_provider_id ON operator_game_branding(game_provider_id);
CREATE INDEX IF NOT EXISTS idx_game_branding_active ON operator_game_branding(is_active);

-- Add comments for documentation
COMMENT ON TABLE operator_branding IS 'Operator branding assets (logos, favicons). Supports PNG, JPG, SVG formats.';
COMMENT ON COLUMN operator_branding.logo_type IS 'Type of logo: OPERATOR_LOGO, FAVICON, MOBILE_LOGO, DESKTOP_LOGO';
COMMENT ON COLUMN operator_branding.file_format IS 'File format: PNG, JPG, JPEG, SVG';

COMMENT ON TABLE operator_game_provider_branding IS 'Game provider branding assets per operator. Supports PNG, JPG, SVG formats.';
COMMENT ON COLUMN operator_game_provider_branding.logo_type IS 'Type of logo: PROVIDER_LOGO, ICON, BANNER';

COMMENT ON TABLE operator_game_branding IS 'Game branding assets per operator. Supports PNG, JPG, SVG formats.';
COMMENT ON COLUMN operator_game_branding.logo_type IS 'Type of logo: GAME_LOGO, THUMBNAIL, BANNER, ICON';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('operator_branding', 'operator_game_provider_branding', 'operator_game_branding')
ORDER BY table_name, ordinal_position;
