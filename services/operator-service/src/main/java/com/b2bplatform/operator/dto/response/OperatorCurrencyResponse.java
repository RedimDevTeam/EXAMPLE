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
public class OperatorCurrencyResponse {
    
    private Long id;
    private Long operatorId;
    private String currencyCode;
    private Boolean isCustom;
    private String currencyName;
    private Boolean isDefault;
    private Boolean isActive;
    private BigDecimal exchangeRate;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
