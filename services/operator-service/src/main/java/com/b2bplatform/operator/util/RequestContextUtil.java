package com.b2bplatform.operator.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Utility class for extracting request context information.
 */
public class RequestContextUtil {
    
    /**
     * Get current HTTP request.
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * Get client IP address from request.
     */
    public static String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs (take the first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
    
    /**
     * Get authenticated user from request.
     */
    public static String getAuthenticatedUser() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "SYSTEM";
        }
        
        // Try to get API key from header
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "API_KEY:" + apiKey.substring(0, Math.min(20, apiKey.length()));
        }
        
        // Try to get username from request attribute (if set by authentication filter)
        Object username = request.getAttribute("username");
        if (username != null) {
            return username.toString();
        }
        
        return "ANONYMOUS";
    }
    
    /**
     * Generate or get request ID.
     */
    public static String getRequestId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return UUID.randomUUID().toString();
        }
        
        // Check if request ID already exists
        Object requestId = request.getAttribute("requestId");
        if (requestId != null) {
            return requestId.toString();
        }
        
        // Generate new request ID
        String newRequestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", newRequestId);
        return newRequestId;
    }
    
    /**
     * Get user agent from request.
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "unknown";
        }
        return request.getHeader("User-Agent");
    }
}
