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
public class ChipDenominationResponse {
    
    private Long id;
    private Long operatorId;
    private String gameId;
    private String gameProviderId;
    private String currencyCode;
    private Integer chipIndex;
    private BigDecimal chipValue;
    private Boolean isActive;
    private Integer displayOrder;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
