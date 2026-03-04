package com.b2bplatform.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for player login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Operator code is required")
    @Size(max = 50, message = "Operator code must not exceed 50 characters")
    private String operatorCode;
    
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 255, message = "Password must be between 1 and 255 characters")
    private String password;
}
