-- Migration: Create operator languages table
-- Date: 2026-02-12
-- Description: Creates table for multi-language support per operator

-- Operator Languages Table
-- Allows operators to support multiple languages (not just baseLanguage)
CREATE TABLE IF NOT EXISTS operator_languages (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL, -- ISO 639-1 language code (e.g., en, es, fr) or custom language code
    is_custom BOOLEAN DEFAULT FALSE, -- TRUE for custom languages, FALSE for ISO 639-1 standard languages
    language_name VARCHAR(100), -- Display name for custom languages (e.g., "Custom Dialect")
    is_default BOOLEAN DEFAULT FALSE, -- One language per operator should be default
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100) NOT NULL, -- Gaming Provider Global Admin username
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operator_languages_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_language_code_format CHECK (
        -- Standard ISO 639-1: exactly 2 lowercase letters
        (is_custom = FALSE AND LENGTH(language_code) = 2 AND language_code = LOWER(language_code)) OR
        -- Custom language: 1-10 alphanumeric characters, lowercase
        (is_custom = TRUE AND LENGTH(language_code) BETWEEN 1 AND 10 AND language_code = LOWER(language_code) AND language_code ~ '^[a-z0-9]+$')
    ),
    CONSTRAINT chk_custom_language_name CHECK (
        -- Custom languages must have a name
        (is_custom = FALSE) OR (is_custom = TRUE AND language_name IS NOT NULL AND LENGTH(TRIM(language_name)) > 0)
    ),
    UNIQUE(operator_id, language_code) -- One language code per operator
);

-- Indexes for operator_languages
CREATE INDEX IF NOT EXISTS idx_operator_languages_operator_id ON operator_languages(operator_id);
CREATE INDEX IF NOT EXISTS idx_operator_languages_language_code ON operator_languages(language_code);
CREATE INDEX IF NOT EXISTS idx_operator_languages_active ON operator_languages(is_active);
CREATE INDEX IF NOT EXISTS idx_operator_languages_default ON operator_languages(operator_id, is_default) WHERE is_default = TRUE;

-- Add comments for documentation
COMMENT ON TABLE operator_languages IS 'Multi-language support for operators. Supports both ISO 639-1 standard languages (en, es, fr) and custom languages.';
COMMENT ON COLUMN operator_languages.operator_id IS 'Operator ID (foreign key to operators table)';
COMMENT ON COLUMN operator_languages.language_code IS 'ISO 639-1 language code (e.g., en, es, fr) or custom language code (e.g., custom1, dialect1, up to 10 characters)';
COMMENT ON COLUMN operator_languages.is_custom IS 'TRUE for custom languages, FALSE for ISO 639-1 standard languages';
COMMENT ON COLUMN operator_languages.language_name IS 'Display name for custom languages (required for custom languages, e.g., "Custom Dialect", "Regional Variant")';
COMMENT ON COLUMN operator_languages.is_default IS 'TRUE if this is the default language for the operator (one per operator)';
COMMENT ON COLUMN operator_languages.is_active IS 'Whether this language is currently active for the operator';

-- Verify table was created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'operator_languages'
ORDER BY ordinal_position;
