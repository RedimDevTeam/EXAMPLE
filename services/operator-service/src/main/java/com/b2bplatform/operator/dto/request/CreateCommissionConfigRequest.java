package com.b2bplatform.operator.dto.request;

import com.b2bplatform.operator.model.CommissionModelType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating commission configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommissionConfigRequest {
    
    @NotBlank(message = "Game provider ID is required")
    @Size(max = 100, message = "Game provider ID must not exceed 100 characters")
    private String gameProviderId;
    
    @Size(max = 100, message = "Game ID must not exceed 100 characters")
    private String gameId; // null = all games
    
    @NotNull(message = "Commission model is required")
    private CommissionModelType commissionModel;
    
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
    
    @NotNull(message = "Effective from date is required")
    private LocalDateTime effectiveFrom;
    
    private LocalDateTime effectiveTo; // null = active indefinitely
}
