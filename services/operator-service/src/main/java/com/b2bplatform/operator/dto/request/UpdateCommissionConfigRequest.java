package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating commission configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommissionConfigRequest {
    
    // GGR-Based fields
    @DecimalMin(value = "0.00", message = "Operator GGR rate must be >= 0")
    @DecimalMax(value = "100.00", message = "Operator GGR rate must be <= 100")
    private BigDecimal operatorGgrRate;
    
    @DecimalMin(value = "0.00", message = "Provider GGR rate must be >= 0")
    @DecimalMax(value = "100.00", message = "Provider GGR rate must be <= 100")
    private BigDecimal providerGgrRate;
    
    // Fixed Price fields
    @DecimalMin(value = "0.01", message = "Fixed price must be > 0")
    private BigDecimal fixedPricePerBet;
    
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase ISO code")
    private String fixedPriceCurrency;
    
    // Winnings-Based fields
    @DecimalMin(value = "0.00", message = "Winnings commission rate must be >= 0")
    @DecimalMax(value = "100.00", message = "Winnings commission rate must be <= 100")
    private BigDecimal winningsCommissionRate;
    
    @DecimalMin(value = "0.00", message = "Operator winnings share must be >= 0")
    @DecimalMax(value = "100.00", message = "Operator winnings share must be <= 100")
    private BigDecimal operatorWinningsShare;
    
    @DecimalMin(value = "0.00", message = "Provider winnings share must be >= 0")
    @DecimalMax(value = "100.00", message = "Provider winnings share must be <= 100")
    private BigDecimal providerWinningsShare;
    
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo;
    
    private Boolean isActive;
}
