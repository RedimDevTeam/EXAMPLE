package com.b2bplatform.bet.client;

import com.b2bplatform.bet.dto.BetRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Synchronous client for Game Service (mock until Game Service is implemented). No WebFlux/Mono.
 */
@Component
@Slf4j
public class GameServiceClient {

    /**
     * Check if game round has started (betting window closed). Sync.
     */
    public GameRoundState checkGameRoundState(String gameCode, String gameRoundId) {
        log.debug("Checking game round state - game: {}, round: {}", gameCode, gameRoundId);
        return GameRoundState.builder()
            .gameCode(gameCode)
            .gameRoundId(gameRoundId)
            .bettingOpen(true)
            .gameStarted(false)
            .gameStartTime(null)
            .build();
    }

    /**
     * Validates bet type and returns odds. Sync. Mock until Game Service is implemented.
     */
    public GameValidationResponse validateBet(String gameCode, BetRequest betRequest) {
        log.debug("Calling Game Service validate bet - game: {}, betType: {}", gameCode, betRequest.getBetType());
        return validateBetMock(gameCode, betRequest);
    }
    
    /**
     * Mock validation until Game Service is implemented.
     * Returns default odds based on bet type.
     */
    private GameValidationResponse validateBetMock(String gameCode, BetRequest betRequest) {
        log.warn("Using mock Game Service validation for game: {}, betType: {}", 
            gameCode, betRequest.getBetType());
        
        // Mock validation - accept all bets for now
        BigDecimal mockOdds = getMockOdds(gameCode, betRequest.getBetType());
        
        return GameValidationResponse.builder()
                .valid(true)
                .odds(mockOdds)
                .maxBetAmount(new java.math.BigDecimal("10000.00"))
                .minBetAmount(new java.math.BigDecimal("0.01"))
                .message("Bet validated successfully (mock)")
                .gameState("BETTING_OPEN")
                .build();
    }
    
    /**
     * Returns mock odds based on game and bet type.
     * This will be replaced by actual Game Service logic.
     */
    private BigDecimal getMockOdds(String gameCode, String betType) {
        // Mock odds for common bet types
        return switch (betType.toUpperCase()) {
            case "PLAYER" -> new BigDecimal("1.0");      // Baccarat Player
            case "BANKER" -> new BigDecimal("0.95");     // Baccarat Banker (with commission)
            case "TIE" -> new BigDecimal("8.0");         // Baccarat Tie
            case "ANTE" -> new BigDecimal("1.0");         // Blackjack Ante
            case "INSURANCE" -> new BigDecimal("2.0");   // Blackjack Insurance
            case "STRAIGHT_UP" -> new BigDecimal("35.0"); // Roulette Straight Up
            case "RED", "BLACK" -> new BigDecimal("1.0"); // Roulette Red/Black
            default -> new BigDecimal("1.0");            // Default odds
        };
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GameValidationResponse {
        private Boolean valid;
        private BigDecimal odds;
        private BigDecimal maxBetAmount;
        private BigDecimal minBetAmount;
        private String message;
        private String gameState;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GameRoundState {
        private String gameCode;
        private String gameRoundId;
        private Boolean bettingOpen;      // true if betting window is open
        private Boolean gameStarted;      // true if game has started
        private java.time.LocalDateTime gameStartTime; // When game started
        
        /**
         * Check if betting window is open
         */
        public boolean isBettingOpen() {
            return Boolean.TRUE.equals(bettingOpen);
        }
        
        /**
         * Check if game has started
         */
        public boolean isGameStarted() {
            return Boolean.TRUE.equals(gameStarted);
        }
    }
}
