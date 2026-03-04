package com.b2bplatform.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO.
 * Supports both HTTP status codes and unified error codes.
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
    
    /**
     * Unified error code (per B2B/B2C Unified Gaming Integration Standard v1.1)
     * Codes: 1000 (Success), 2001 (Insufficient Funds), 2002 (Account Blocked),
     *        3001 (Duplicate Reference), 4001 (Token Invalid), 5000 (System Error)
     */
    private Integer errorCode;
}
