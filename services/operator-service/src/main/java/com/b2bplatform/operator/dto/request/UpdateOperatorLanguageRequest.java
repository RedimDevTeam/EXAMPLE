package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOperatorLanguageRequest {
    
    @Size(max = 100, message = "Language name must not exceed 100 characters")
    private String languageName; // Can update name for custom languages
    
    private Boolean isDefault;
    
    private Boolean isActive;
}
