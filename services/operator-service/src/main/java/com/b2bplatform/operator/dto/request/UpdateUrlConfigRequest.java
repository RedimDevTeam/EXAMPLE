package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating operator URL configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUrlConfigRequest {
    
    @Size(max = 500, message = "Request URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.+|)$", message = "Request URL must be a valid HTTP/HTTPS URL or empty")
    private String requestUrl;
    
    @Size(max = 200, message = "Directory path must not exceed 200 characters")
    @Pattern(regexp = "^(/[^\\s]*|)$", message = "Directory path must start with / or be empty")
    private String directoryPath;
    
    @Size(max = 200, message = "Virtual path must not exceed 200 characters")
    @Pattern(regexp = "^(/[^\\s]*|)$", message = "Virtual path must start with / or be empty")
    private String virtualPath;
}
