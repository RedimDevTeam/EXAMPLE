package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameBetLimitResponse {
    
    private Long id;
    private String gameId;
    private String gameProviderId;
    private String currencyCode;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private Boolean isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
