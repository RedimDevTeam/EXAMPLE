package com.b2bplatform.operator.aspect;

import com.b2bplatform.operator.service.OperatorAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * AOP Aspect for automatically logging API access.
 * Logs all requests to admin endpoints.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAccessLoggingAspect {
    
    private final OperatorAuditService auditService;
    
    /**
     * Log API access for all admin endpoints.
     */
    @Around("execution(* com.b2bplatform.operator.controller..*.*(..))")
    public Object logApiAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();
        String requestIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestId = UUID.randomUUID().toString();
        
        // Extract operator ID from path if present
        Long operatorId = extractOperatorIdFromPath(endpoint);
        
        // Extract authenticated user (API key or username)
        String authenticatedBy = extractAuthenticatedUser(request);
        
        long startTime = System.currentTimeMillis();
        Integer httpStatus = 200;
        String errorMessage = null;
        
        try {
            Object result = joinPoint.proceed();
            
            // Try to extract HTTP status from response
            if (result instanceof org.springframework.http.ResponseEntity) {
                org.springframework.http.ResponseEntity<?> response = (org.springframework.http.ResponseEntity<?>) result;
                httpStatus = response.getStatusCode().value();
            }
            
            return result;
        } catch (Exception e) {
            httpStatus = 500;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log API access asynchronously (non-blocking)
            try {
                auditService.logApiAccess(
                    operatorId,
                    endpoint,
                    httpMethod,
                    httpStatus,
                    requestIp,
                    userAgent,
                    authenticatedBy,
                    requestId,
                    responseTime,
                    errorMessage
                );
            } catch (Exception e) {
                log.error("Failed to log API access: {}", e.getMessage());
                // Don't throw - audit logging should not break business logic
            }
        }
    }
    
    /**
     * Extract operator ID from request path.
     */
    private Long extractOperatorIdFromPath(String path) {
        try {
            // Pattern: /api/v1/admin/operators/{operatorId}/...
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("operators".equals(parts[i]) && i + 1 < parts.length) {
                    String operatorIdStr = parts[i + 1];
                    // Check if it's a number (not a string like "code" or "maintenance")
                    if (operatorIdStr.matches("\\d+")) {
                        return Long.parseLong(operatorIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract operator ID from path: {}", path);
        }
        return null;
    }
    
    /**
     * Extract authenticated user from request.
     */
    private String extractAuthenticatedUser(HttpServletRequest request) {
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
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
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
}
