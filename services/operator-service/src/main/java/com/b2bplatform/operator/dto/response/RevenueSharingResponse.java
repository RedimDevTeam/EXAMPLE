package com.b2bplatform.operator.dto.response;

import com.b2bplatform.operator.model.RevenueType;
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
public class RevenueSharingResponse {
    
    private Long id;
    private Long operatorId;
    private Long parentOperatorId;
    private RevenueType revenueType;
    private BigDecimal parentSharePercentage;
    private BigDecimal operatorSharePercentage;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
