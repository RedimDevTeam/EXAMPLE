package com.b2bplatform.wallet.dto.b2b;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kickout User Request DTO
 * Uses industry-standard field naming conventions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KickoutRequest {
    
    /**
     * Operator code/identifier (Industry standard - String)
     */
    @NotBlank(message = "Operator code is required")
    @Size(min = 3, max = 50, message = "Operator code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Operator code must contain only uppercase letters, numbers, and underscores")
    private String operatorCode;
    
    /**
     * Player identifier
     */
    @NotBlank(message = "Player ID is required")
    @Size(max = 100, message = "Player ID must not exceed 100 characters")
    private String playerId;
}
