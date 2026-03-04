package com.b2bplatform.b2c.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private Integer status; // HTTP status code
    private String error;
    private String message;
    private String path;
}
