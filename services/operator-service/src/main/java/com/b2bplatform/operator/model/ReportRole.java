package com.b2bplatform.operator.model;

/**
 * Enum for report access roles (hierarchical).
 */
public enum ReportRole {
    CASINO_ADMIN,    // Lowest level - Casino-specific admin
    GROUP_ADMIN,     // Middle level - Group of casinos admin
    GLOBAL_ADMIN     // Highest level - Global admin (all operators)
}
