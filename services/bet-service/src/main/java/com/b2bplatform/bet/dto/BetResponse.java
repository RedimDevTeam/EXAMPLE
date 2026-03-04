package com.b2bplatform.bet.dto;

import com.b2bplatform.bet.model.Bet;
import com.b2bplatform.bet.model.BetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetResponse {
    private String betId;
    private Long playerId;
    private Long operatorId;
    private String gameCode;
    private String gameRoundId;
    private String betCategory;
    private String betType;
    private BigDecimal betAmount;
    private String currency;
    private BetStatus status;
    private BigDecimal odds;
    private BigDecimal payoutAmount;
    private Map<String, Object> betDetails;
    private String walletTransactionId;
    private LocalDateTime operatorPostedAt;
    private LocalDateTime operatorResponseAt;
    private LocalDateTime gameStartedAt;
    private Map<String, Object> operatorResponse;
    private String operatorResponseMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime settledAt;
    
    public static BetResponse from(Bet bet) {
        return BetResponse.builder()
                .betId(bet.getBetId())
                .playerId(bet.getPlayerId())
                .operatorId(bet.getOperatorId())
                .gameCode(bet.getGameCode())
                .gameRoundId(bet.getGameRoundId())
                .betCategory(bet.getBetCategory())
                .betType(bet.getBetType())
                .betAmount(bet.getBetAmount())
                .currency(bet.getCurrency())
                .status(bet.getStatus())
                .odds(bet.getOdds())
                .payoutAmount(bet.getPayoutAmount())
                .betDetails(bet.getBetDetails())
                .walletTransactionId(bet.getWalletTransactionId())
                .operatorPostedAt(bet.getOperatorPostedAt())
                .operatorResponseAt(bet.getOperatorResponseAt())
                .gameStartedAt(bet.getGameStartedAt())
                .operatorResponse(bet.getOperatorResponse())
                .operatorResponseMessage(bet.getOperatorResponseMessage())
                .createdAt(bet.getCreatedAt())
                .updatedAt(bet.getUpdatedAt())
                .settledAt(bet.getSettledAt())
                .build();
    }
}
