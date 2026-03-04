package com.b2bplatform.operator.dto.response;

import com.b2bplatform.operator.model.AllocationType;
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
public class CreditAllocationResponse {
    
    private Long id;
    private Long parentOperatorId;
    private Long childOperatorId;
    private BigDecimal creditLimit;
    private BigDecimal usedCredit;
    private BigDecimal availableCredit;
    private String currencyCode;
    private AllocationType allocationType;
    private Boolean autoReplenish;
    private BigDecimal replenishThreshold;
    private Boolean isActive;
    private LocalDateTime allocatedAt;
    private LocalDateTime expiresAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
