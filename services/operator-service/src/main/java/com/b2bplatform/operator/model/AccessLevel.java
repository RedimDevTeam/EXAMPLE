package com.b2bplatform.operator.model;

/**
 * Enum for access levels.
 */
public enum AccessLevel {
    READ_ONLY,      // Can only view reports
    READ_WRITE,     // Can view and modify reports
    FULL_ACCESS     // Full access including deletion
}
