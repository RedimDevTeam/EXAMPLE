-- Migration: Create operator hierarchy tables
-- Date: 2026-02-12
-- Description: Creates tables for Master → Agent → Sub-Agent hierarchy with revenue sharing and credit allocation

-- Operator Hierarchy Table
-- Stores hierarchical relationships: Master → Agent → Sub-Agent
CREATE TABLE IF NOT EXISTS operator_hierarchy (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL UNIQUE, -- Each operator has one hierarchy entry
    parent_operator_id BIGINT, -- Parent operator (null for Master operators)
    hierarchy_level INTEGER NOT NULL CHECK (hierarchy_level BETWEEN 1 AND 3), -- 1=Master, 2=Agent, 3=Sub-Agent
    hierarchy_path VARCHAR(500), -- Full path (e.g., "1/5/10" for Master->Agent->SubAgent)
    is_master BOOLEAN DEFAULT FALSE, -- True for Master operators
    is_agent BOOLEAN DEFAULT FALSE, -- True for Agent operators
    is_sub_agent BOOLEAN DEFAULT FALSE, -- True for Sub-Agent operators
    can_create_children BOOLEAN DEFAULT FALSE, -- Permission to create child operators
    max_children_count INTEGER, -- Maximum number of child operators allowed (null = unlimited)
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hierarchy_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT fk_hierarchy_parent FOREIGN KEY (parent_operator_id) REFERENCES operators(id) ON DELETE SET NULL,
    CONSTRAINT chk_hierarchy_level CHECK (hierarchy_level >= 1 AND hierarchy_level <= 3),
    CONSTRAINT chk_hierarchy_type CHECK (
        (hierarchy_level = 1 AND is_master = TRUE AND parent_operator_id IS NULL) OR
        (hierarchy_level = 2 AND is_agent = TRUE AND parent_operator_id IS NOT NULL) OR
        (hierarchy_level = 3 AND is_sub_agent = TRUE AND parent_operator_id IS NOT NULL)
    )
);

-- Revenue Sharing Configuration Table
-- Stores revenue sharing percentages for each hierarchy level
CREATE TABLE IF NOT EXISTS operator_revenue_sharing (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    parent_operator_id BIGINT NOT NULL, -- Parent operator receiving revenue share
    revenue_type VARCHAR(50) NOT NULL CHECK (revenue_type IN ('GGR', 'NET_REVENUE', 'COMMISSION', 'BET_VOLUME')), -- Type of revenue
    parent_share_percentage DECIMAL(5,2) NOT NULL CHECK (parent_share_percentage >= 0 AND parent_share_percentage <= 100), -- Percentage to parent
    operator_share_percentage DECIMAL(5,2) NOT NULL CHECK (operator_share_percentage >= 0 AND operator_share_percentage <= 100), -- Percentage to operator
    effective_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- When this sharing rule becomes effective
    effective_to TIMESTAMP, -- When this sharing rule expires (null = indefinite)
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_revenue_sharing_operator FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT fk_revenue_sharing_parent FOREIGN KEY (parent_operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_revenue_sharing_sum CHECK (parent_share_percentage + operator_share_percentage <= 100),
    UNIQUE(operator_id, parent_operator_id, revenue_type, effective_from) -- One active rule per operator-parent-revenue type
);

-- Credit Allocation Table
-- Stores credit limits allocated by parent operators to child operators
CREATE TABLE IF NOT EXISTS operator_credit_allocation (
    id BIGSERIAL PRIMARY KEY,
    parent_operator_id BIGINT NOT NULL, -- Parent operator allocating credit
    child_operator_id BIGINT NOT NULL, -- Child operator receiving credit
    credit_limit DECIMAL(19,2) NOT NULL CHECK (credit_limit >= 0), -- Total credit limit allocated
    used_credit DECIMAL(19,2) DEFAULT 0 CHECK (used_credit >= 0), -- Credit currently used
    available_credit DECIMAL(19,2) GENERATED ALWAYS AS (credit_limit - used_credit) STORED, -- Available credit (calculated)
    currency_code VARCHAR(10) NOT NULL, -- Currency for this credit allocation
    allocation_type VARCHAR(20) DEFAULT 'MANUAL' CHECK (allocation_type IN ('MANUAL', 'AUTO', 'PERCENTAGE')), -- How credit is allocated
    auto_replenish BOOLEAN DEFAULT FALSE, -- Auto-replenish when credit is low
    replenish_threshold DECIMAL(5,2), -- Percentage threshold for auto-replenish (e.g., 20% = replenish when below 20%)
    is_active BOOLEAN DEFAULT TRUE,
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP, -- Optional credit expiration
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_allocation_parent FOREIGN KEY (parent_operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_allocation_child FOREIGN KEY (child_operator_id) REFERENCES operators(id) ON DELETE CASCADE,
    CONSTRAINT chk_credit_used_limit CHECK (used_credit <= credit_limit),
    UNIQUE(parent_operator_id, child_operator_id, currency_code) -- One allocation per parent-child-currency combination
);

-- Indexes for operator_hierarchy
CREATE INDEX IF NOT EXISTS idx_hierarchy_operator_id ON operator_hierarchy(operator_id);
CREATE INDEX IF NOT EXISTS idx_hierarchy_parent_id ON operator_hierarchy(parent_operator_id);
CREATE INDEX IF NOT EXISTS idx_hierarchy_level ON operator_hierarchy(hierarchy_level);
CREATE INDEX IF NOT EXISTS idx_hierarchy_path ON operator_hierarchy(hierarchy_path);

-- Indexes for operator_revenue_sharing
CREATE INDEX IF NOT EXISTS idx_revenue_sharing_operator_id ON operator_revenue_sharing(operator_id);
CREATE INDEX IF NOT EXISTS idx_revenue_sharing_parent_id ON operator_revenue_sharing(parent_operator_id);
CREATE INDEX IF NOT EXISTS idx_revenue_sharing_active ON operator_revenue_sharing(is_active);
CREATE INDEX IF NOT EXISTS idx_revenue_sharing_effective ON operator_revenue_sharing(effective_from, effective_to);

-- Indexes for operator_credit_allocation
CREATE INDEX IF NOT EXISTS idx_credit_allocation_parent_id ON operator_credit_allocation(parent_operator_id);
CREATE INDEX IF NOT EXISTS idx_credit_allocation_child_id ON operator_credit_allocation(child_operator_id);
CREATE INDEX IF NOT EXISTS idx_credit_allocation_active ON operator_credit_allocation(is_active);
CREATE INDEX IF NOT EXISTS idx_credit_allocation_currency ON operator_credit_allocation(currency_code);

-- Add comments for documentation
COMMENT ON TABLE operator_hierarchy IS 'Operator hierarchy: Master (level 1) → Agent (level 2) → Sub-Agent (level 3). Supports up to 3 levels.';
COMMENT ON COLUMN operator_hierarchy.hierarchy_level IS 'Hierarchy level: 1=Master, 2=Agent, 3=Sub-Agent';
COMMENT ON COLUMN operator_hierarchy.hierarchy_path IS 'Full hierarchy path (e.g., "1/5/10" represents Master ID 1 → Agent ID 5 → Sub-Agent ID 10)';
COMMENT ON COLUMN operator_hierarchy.max_children_count IS 'Maximum number of child operators allowed (null = unlimited)';

COMMENT ON TABLE operator_revenue_sharing IS 'Revenue sharing configuration between parent and child operators. Supports multiple revenue types and time-based rules.';
COMMENT ON COLUMN operator_revenue_sharing.revenue_type IS 'Type of revenue: GGR (Gross Gaming Revenue), NET_REVENUE, COMMISSION, BET_VOLUME';
COMMENT ON COLUMN operator_revenue_sharing.parent_share_percentage IS 'Percentage of revenue shared with parent operator (0-100)';
COMMENT ON COLUMN operator_revenue_sharing.operator_share_percentage IS 'Percentage of revenue retained by operator (0-100)';

COMMENT ON TABLE operator_credit_allocation IS 'Credit allocation from parent operators to child operators. Tracks credit limits, usage, and auto-replenishment.';
COMMENT ON COLUMN operator_credit_allocation.credit_limit IS 'Total credit limit allocated to child operator';
COMMENT ON COLUMN operator_credit_allocation.used_credit IS 'Credit currently used by child operator';
COMMENT ON COLUMN operator_credit_allocation.available_credit IS 'Available credit (calculated: credit_limit - used_credit)';
COMMENT ON COLUMN operator_credit_allocation.allocation_type IS 'Allocation method: MANUAL, AUTO (automatic), PERCENTAGE (percentage-based)';

-- Verify tables were created
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_name IN ('operator_hierarchy', 'operator_revenue_sharing', 'operator_credit_allocation')
ORDER BY table_name, ordinal_position;
