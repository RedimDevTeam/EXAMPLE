package com.b2bplatform.bet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "bets", indexes = {
    @Index(name = "idx_bet_id", columnList = "bet_id"),
    @Index(name = "idx_player_id", columnList = "player_id"),
    @Index(name = "idx_operator_id", columnList = "operator_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_game_code", columnList = "game_code"),
    @Index(name = "idx_bet_category", columnList = "bet_category"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bet_id", unique = true, nullable = false, length = 100)
    private String betId;
    
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    @Column(name = "game_code", nullable = false, length = 50)
    private String gameCode;
    
    @Column(name = "game_round_id", length = 100)
    private String gameRoundId;
    
    @Column(name = "bet_category", nullable = false, length = 50)
    private String betCategory; // "MAIN_BET" or "SIDE_BET"
    
    @Column(name = "bet_type", nullable = false, length = 100)
    private String betType; // Game-specific: "PLAYER", "BANKER", "ANTE", etc.
    
    @Column(name = "bet_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal betAmount;
    
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BetStatus status;
    
    @Column(name = "odds", precision = 10, scale = 4)
    private BigDecimal odds; // Provided by Game Service
    
    @Column(name = "payout_amount", precision = 18, scale = 2)
    private BigDecimal payoutAmount; // Calculated by Game Service
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bet_details", columnDefinition = "jsonb")
    private Map<String, Object> betDetails; // Flexible game-specific data
    
    @Column(name = "wallet_transaction_id", length = 100)
    private String walletTransactionId;
    
    @Column(name = "operator_posted_at")
    private LocalDateTime operatorPostedAt; // When bet was posted to operator
    
    @Column(name = "operator_response_at")
    private LocalDateTime operatorResponseAt; // When operator responded
    
    @Column(name = "game_started_at")
    private LocalDateTime gameStartedAt; // When game round started
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "operator_response", columnDefinition = "jsonb")
    private Map<String, Object> operatorResponse; // Operator response payload
    
    @Column(name = "operator_response_message", length = 500)
    private String operatorResponseMessage; // Error message from operator (low balance, not a player, etc.)
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "settled_at")
    private LocalDateTime settledAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
