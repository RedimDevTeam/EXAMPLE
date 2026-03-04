package com.b2bplatform.bet.model;

public enum BetStatus {
    PENDING,         // Bet is being processed, waiting for operator confirmation
    ACCEPTED,        // Bet accepted and confirmed by operator BEFORE game start
    NOT_CONFIRMED,   // Bet not confirmed by operator before game start (auto-rejected)
    SETTLED,         // Bet settled and wallet credited
    CANCELLED,       // Bet cancelled and wallet refunded
    REJECTED         // Bet rejected (validation failed, wallet failed, operator rejected, etc.)
}
