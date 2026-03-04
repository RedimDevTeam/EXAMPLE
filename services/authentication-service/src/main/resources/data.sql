-- Sample test data for Authentication Service
-- Note: This file is only executed if spring.jpa.hibernate.ddl-auto is set to create or create-drop
-- For update mode, run these SQL commands manually

-- Password for all test users: test123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Insert test player for operator ID 1 (OP001)
-- Make sure operator with ID 1 exists first
INSERT INTO player_credentials (operator_id, player_id, username, password_hash, status, created_at, updated_at)
SELECT 
    1,
    'player_001',
    'testplayer',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM player_credentials WHERE operator_id = 1 AND username = 'testplayer'
);
