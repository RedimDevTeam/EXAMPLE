package com.b2bplatform.operator.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for bulk creating chip denominations (e.g., from Excel upload).
 * Supports creating all 7 chips (index 0-6) at once.
 */
@Data
public class BulkCreateChipDenominationsRequest {
    
    @NotBlank(message = "Game provider ID is required")
    @Size(max = 100, message = "Game provider ID must not exceed 100 characters")
    private String gameProviderId;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 1, max = 10, message = "Currency code must be between 1 and 10 characters")
    private String currencyCode;
    
    @NotNull(message = "Chip denominations are required")
    @Size(min = 1, max = 20, message = "Must provide between 1 and 20 chip denominations")
    @Valid
    private List<CreateChipDenominationRequest> chipDenominations; // Flexible: supports any number of chips (e.g., 5, 6, 7) based on UI space
}
