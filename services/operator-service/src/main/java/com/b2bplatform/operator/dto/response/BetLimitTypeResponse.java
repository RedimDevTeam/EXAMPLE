package com.b2bplatform.operator.dto.response;

import com.b2bplatform.operator.model.BetLimitType;
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
public class BetLimitTypeResponse {
    
    private Long id;
    private Long operatorId;
    private String gameId;
    private String gameProviderId;
    private String currencyCode;
    private BetLimitType limitType;
    private BigDecimal minBetLimit;
    private BigDecimal maxBetLimit;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
