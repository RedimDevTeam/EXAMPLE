package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateApiCredentialsRequest;
import com.b2bplatform.operator.dto.request.UpdateApiCredentialsRequest;
import com.b2bplatform.operator.dto.response.ApiCredentialsResponse;
import com.b2bplatform.operator.model.OperatorApiCredentials;
import com.b2bplatform.operator.repository.OperatorApiCredentialsRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing API access credentials (username/password).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCredentialsService {
    
    private final OperatorApiCredentialsRepository credentialsRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    @Transactional
    public ApiCredentialsResponse createCredentials(Long operatorId, CreateApiCredentialsRequest request) {
        log.info("Creating API credentials for operator {}, username {}", operatorId, request.getUsername());
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Check if credentials already exist for operator
        if (credentialsRepository.existsByOperatorId(operatorId)) {
            throw new IllegalStateException("API credentials already exist for operator: " + operatorId);
        }
        
        // Check if username is already taken
        if (credentialsRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Username already exists: " + request.getUsername());
        }
        
        // Hash password
        String passwordHash = passwordEncoder.encode(request.getPassword());
        
        // Create credentials
        OperatorApiCredentials credentials = new OperatorApiCredentials();
        credentials.setOperatorId(operatorId);
        credentials.setUsername(request.getUsername());
        credentials.setPasswordHash(passwordHash);
        credentials.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorApiCredentials saved = credentialsRepository.save(credentials);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("username", saved.getUsername());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            operatorId,
            "API_CREDENTIALS_CREATED",
            String.format("Created API credentials for operator %d, username %s", operatorId, request.getUsername()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    public ApiCredentialsResponse getCredentials(Long operatorId) {
        log.info("Getting API credentials for operator {}", operatorId);
        
        OperatorApiCredentials credentials = credentialsRepository.findByOperatorId(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("API credentials not found for operator: " + operatorId));
        
        return mapToResponse(credentials);
    }
    
    @Transactional
    public ApiCredentialsResponse updateCredentials(Long operatorId, UpdateApiCredentialsRequest request) {
        log.info("Updating API credentials for operator {}", operatorId);
        
        OperatorApiCredentials credentials = credentialsRepository.findByOperatorId(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("API credentials not found for operator: " + operatorId));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isActive", credentials.getIsActive());
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            credentials.setPasswordChangedAt(LocalDateTime.now());
        }
        
        // Update active status if provided
        if (request.getIsActive() != null) {
            credentials.setIsActive(request.getIsActive());
        }
        
        credentials.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorApiCredentials saved = credentialsRepository.save(credentials);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isActive", saved.getIsActive());
        if (request.getPassword() != null) {
            newValues.put("passwordChanged", true);
        }
        
        auditService.logAuditEvent(
            operatorId,
            "API_CREDENTIALS_UPDATED",
            String.format("Updated API credentials for operator %d", operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToResponse(saved);
    }
    
    /**
     * Validate credentials (for authentication).
     */
    public boolean validateCredentials(String username, String password) {
        log.debug("Validating credentials for username {}", username);
        
        OperatorApiCredentials credentials = credentialsRepository.findByUsername(username)
            .orElse(null);
        
        if (credentials == null) {
            return false;
        }
        
        // Check if locked
        if (credentials.getIsLocked()) {
            if (credentials.getLockedUntil() != null && LocalDateTime.now().isBefore(credentials.getLockedUntil())) {
                log.warn("Account locked until {}", credentials.getLockedUntil());
                return false;
            } else {
                // Lock expired, unlock
                credentials.setIsLocked(false);
                credentials.setFailedLoginAttempts(0);
                credentials.setLockedUntil(null);
                credentialsRepository.save(credentials);
            }
        }
        
        // Check if active
        if (!credentials.getIsActive()) {
            log.warn("Account is inactive");
            return false;
        }
        
        // Check if expired
        if (credentials.getExpiresAt() != null && LocalDateTime.now().isAfter(credentials.getExpiresAt())) {
            log.warn("Credentials expired");
            return false;
        }
        
        // Validate password
        boolean isValid = passwordEncoder.matches(password, credentials.getPasswordHash());
        
        if (isValid) {
            // Reset failed attempts and update last login
            credentials.setFailedLoginAttempts(0);
            credentials.setLastLoginAt(LocalDateTime.now());
            credentials.setLockedUntil(null);
            credentialsRepository.save(credentials);
        } else {
            // Increment failed attempts
            int attempts = credentials.getFailedLoginAttempts() + 1;
            credentials.setFailedLoginAttempts(attempts);
            
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                credentials.setIsLocked(true);
                credentials.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
                log.warn("Account locked after {} failed attempts", attempts);
            }
            
            credentialsRepository.save(credentials);
        }
        
        return isValid;
    }
    
    private ApiCredentialsResponse mapToResponse(OperatorApiCredentials credentials) {
        return ApiCredentialsResponse.builder()
            .id(credentials.getId())
            .operatorId(credentials.getOperatorId())
            .username(credentials.getUsername())
            .isActive(credentials.getIsActive())
            .isLocked(credentials.getIsLocked())
            .failedLoginAttempts(credentials.getFailedLoginAttempts())
            .lockedUntil(credentials.getLockedUntil())
            .lastLoginAt(credentials.getLastLoginAt())
            .passwordChangedAt(credentials.getPasswordChangedAt())
            .expiresAt(credentials.getExpiresAt())
            .createdBy(credentials.getCreatedBy())
            .createdAt(credentials.getCreatedAt())
            .updatedBy(credentials.getUpdatedBy())
            .updatedAt(credentials.getUpdatedAt())
            .build();
    }
}
