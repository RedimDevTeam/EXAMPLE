-- Migration: Create operator UI settings table
-- Date: 2026-02-12
-- Description: Creates table for operator-specific UI configuration settings

-- Operator UI Settings Table
-- Stores 20+ UI configuration settings per operator
CREATE TABLE IF NOT EXISTS operator_ui_settings (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL UNIQUE, -- One UI settings record per operator
    -- Video/Streaming Settings
    only_jsmpeg BOOLEAN DEFAULT FALSE, -- Use only JSMpeg for video streaming
    allow_full_screen BOOLEAN DEFAULT TRUE, -- Allow full screen mode
    auto_play_video BOOLEAN DEFAULT FALSE, -- Auto-play video streams
    
    -- Display Settings
    show_currency BOOLEAN DEFAULT TRUE, -- Show currency symbol/name
    show_balance BOOLEAN DEFAULT TRUE, -- Show player balance
    show_game_history BOOLEAN DEFAULT TRUE, -- Show game history
    show_bet_history BOOLEAN DEFAULT TRUE, -- Show bet history
    show_statistics BOOLEAN DEFAULT FALSE, -- Show game statistics
    
    -- UI Behavior Settings
    enable_sound_effects BOOLEAN DEFAULT TRUE, -- Enable sound effects
    enable_background_music BOOLEAN DEFAULT FALSE, -- Enable background music
    enable_animations BOOLEAN DEFAULT TRUE, -- Enable UI animations
    enable_tooltips BOOLEAN DEFAULT TRUE, -- Show tooltips
    
    -- Layout Settings
    show_side_panel BOOLEAN DEFAULT TRUE, -- Show side panel
    show_top_bar BOOLEAN DEFAULT TRUE, -- Show top navigation bar
    show_bottom_bar BOOLEAN DEFAULT TRUE, -- Show bottom bar
    compact_mode BOOLEAN DEFAULT FALSE, -- Use compact UI mode
    
    -- Game Settings
    show_game_info BOOLEAN DEFAULT TRUE, -- Show game information
    show_rules BOOLEAN DEFAULT TRUE, -- Show game rules
    show_payout_table BOOLEAN DEFAULT TRUE, -- Show payout table
    enable_chat BOOLEAN DEFAULT FALSE, -- Enable chat feature
    
    -- Responsive Settings
    mobile_optimized BOOLEAN DEFAULT TRUE, -- Mobile-optimized layout
    tablet_optimized BOOLEAN DEFAULT TRUE, -- Tablet-optimized layout
    desktop_optimized BOOLEAN DEFAULT TRUE, -- Desktop-optimized layout
    
    -- Theme Settings
    theme_color VARCHAR(50), -- Primary theme color (hex code)
    background_color VARCHAR(50), -- Background color (hex code)
    font_family VARCHAR(100), -- Font family
    font_size VARCHAR(20), -- Font size (e.g., "14px", "medium")
    
    -- Other Settings
    language_selector_enabled BOOLEAN DEFAULT TRUE, -- Show language selector
    currency_selector_enabled BOOLEAN DEFAULT TRUE, -- Show currency selector
    logout_button_enabled BOOLEAN DEFAULT TRUE, -- Show logout button
    
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ui_settings_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE
);

-- Indexes for operator_ui_settings
CREATE INDEX IF NOT EXISTS idx_ui_settings_operator_id ON operator_ui_settings(operator_id);

-- Add comments for documentation
COMMENT ON TABLE operator_ui_settings IS 'Operator-specific UI configuration settings. Controls UI behavior, display options, and theme settings.';
COMMENT ON COLUMN operator_ui_settings.only_jsmpeg IS 'Use only JSMpeg for video streaming (alternative to other streaming protocols)';
COMMENT ON COLUMN operator_ui_settings.show_currency IS 'Display currency symbol/name in UI';
COMMENT ON COLUMN operator_ui_settings.allow_full_screen IS 'Allow users to enter full screen mode';
COMMENT ON COLUMN operator_ui_settings.theme_color IS 'Primary theme color in hex format (e.g., #FF5733)';
COMMENT ON COLUMN operator_ui_settings.background_color IS 'Background color in hex format (e.g., #FFFFFF)';

-- Verify table was created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'operator_ui_settings'
ORDER BY ordinal_position;
