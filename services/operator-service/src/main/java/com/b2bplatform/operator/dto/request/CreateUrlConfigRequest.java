package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating operator URL configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUrlConfigRequest {
    
    @Size(max = 500, message = "Request URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.+|)$", message = "Request URL must be a valid HTTP/HTTPS URL or empty")
    private String requestUrl; // Operator API base URL
    
    @Size(max = 200, message = "Directory path must not exceed 200 characters")
    @Pattern(regexp = "^(/[^\\s]*|)$", message = "Directory path must start with / or be empty")
    private String directoryPath; // Application directory mapping
    
    @Size(max = 200, message = "Virtual path must not exceed 200 characters")
    @Pattern(regexp = "^(/[^\\s]*|)$", message = "Virtual path must start with / or be empty")
    private String virtualPath; // Reverse proxy / routing path
}
