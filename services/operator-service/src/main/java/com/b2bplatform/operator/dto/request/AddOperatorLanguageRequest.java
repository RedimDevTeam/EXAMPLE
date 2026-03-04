package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddOperatorLanguageRequest {
    
    @NotBlank(message = "Language code is required")
    @Size(min = 1, max = 10, message = "Language code must be between 1 and 10 characters")
    @Pattern(regexp = "^[a-z0-9]+$", message = "Language code must contain only lowercase letters and numbers")
    private String languageCode;
    
    private Boolean isCustom = false; // TRUE for custom languages, FALSE for ISO 639-1 standard
    
    @Size(max = 100, message = "Language name must not exceed 100 characters")
    private String languageName; // Required for custom languages, optional for standard
    
    private Boolean isDefault = false;
}
