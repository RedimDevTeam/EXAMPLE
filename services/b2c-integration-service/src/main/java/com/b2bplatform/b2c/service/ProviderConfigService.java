package com.b2bplatform.b2c.service;

import com.b2bplatform.b2c.dto.request.CreateProviderConfigRequest;
import com.b2bplatform.b2c.dto.request.UpdateProviderConfigRequest;
import com.b2bplatform.b2c.dto.response.ProviderConfigResponse;
import com.b2bplatform.b2c.exception.ResourceNotFoundException;
import com.b2bplatform.b2c.model.ProviderConfig;
import com.b2bplatform.b2c.repository.ProviderConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing B2C provider configurations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderConfigService {
    
    private final ProviderConfigRepository providerConfigRepository;
    
    /**
     * Create a new provider configuration
     */
    @Transactional
    public ProviderConfigResponse createProvider(CreateProviderConfigRequest request) {
        log.info("Creating provider config: providerId={}", request.getProviderId());
        
        // Check if provider ID already exists
        if (providerConfigRepository.existsByProviderId(request.getProviderId())) {
            throw new IllegalArgumentException("Provider with ID '" + request.getProviderId() + "' already exists");
        }
        
        // Validate HMAC auth requires secret
        if ("HMAC".equals(request.getAuthType()) && 
            (request.getApiSecret() == null || request.getApiSecret().isBlank())) {
            throw new IllegalArgumentException("API secret is required for HMAC authentication");
        }
        
        ProviderConfig providerConfig = ProviderConfig.builder()
                .providerId(request.getProviderId())
                .providerName(request.getProviderName())
                .apiBaseUrl(request.getApiBaseUrl())
                .apiKey(request.getApiKey())
                .apiSecret(request.getApiSecret())
                .authType(request.getAuthTypeEnum())
                .supportsXml(request.getSupportsXml())
                .supportsJson(request.getSupportsJson())
                .timeoutMs(request.getTimeoutMs())
                .retryAttempts(request.getRetryAttempts())
                .isActive(request.getIsActive())
                .build();
        
        providerConfig = providerConfigRepository.save(providerConfig);
        log.info("Provider config created: id={}, providerId={}", providerConfig.getId(), providerConfig.getProviderId());
        
        return toResponse(providerConfig);
    }
    
    /**
     * Get provider configuration by provider ID
     */
    @Transactional(readOnly = true)
    public ProviderConfigResponse getProvider(String providerId) {
        log.debug("Getting provider config: providerId={}", providerId);
        
        ProviderConfig providerConfig = providerConfigRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerId));
        
        return toResponse(providerConfig);
    }
    
    /**
     * Get provider configuration entity by provider ID (internal use)
     */
    @Transactional(readOnly = true)
    public ProviderConfig getProviderEntity(String providerId) {
        return providerConfigRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerId));
    }
    
    /**
     * Update provider configuration
     */
    @Transactional
    public ProviderConfigResponse updateProvider(String providerId, UpdateProviderConfigRequest request) {
        log.info("Updating provider config: providerId={}", providerId);
        
        ProviderConfig providerConfig = providerConfigRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerId));
        
        // Update fields if provided
        if (request.getProviderName() != null) {
            providerConfig.setProviderName(request.getProviderName());
        }
        if (request.getApiBaseUrl() != null) {
            providerConfig.setApiBaseUrl(request.getApiBaseUrl());
        }
        if (request.getApiKey() != null) {
            providerConfig.setApiKey(request.getApiKey());
        }
        if (request.getApiSecret() != null) {
            providerConfig.setApiSecret(request.getApiSecret());
        }
        if (request.getAuthTypeEnum() != null) {
            providerConfig.setAuthType(request.getAuthTypeEnum());
            // Validate HMAC requires secret
            if (request.getAuthTypeEnum() == ProviderConfig.AuthType.HMAC && 
                (providerConfig.getApiSecret() == null || providerConfig.getApiSecret().isBlank())) {
                throw new IllegalArgumentException("API secret is required for HMAC authentication");
            }
        }
        if (request.getSupportsXml() != null) {
            providerConfig.setSupportsXml(request.getSupportsXml());
        }
        if (request.getSupportsJson() != null) {
            providerConfig.setSupportsJson(request.getSupportsJson());
        }
        if (request.getTimeoutMs() != null) {
            providerConfig.setTimeoutMs(request.getTimeoutMs());
        }
        if (request.getRetryAttempts() != null) {
            providerConfig.setRetryAttempts(request.getRetryAttempts());
        }
        if (request.getIsActive() != null) {
            providerConfig.setIsActive(request.getIsActive());
        }
        
        providerConfig = providerConfigRepository.save(providerConfig);
        log.info("Provider config updated: providerId={}", providerId);
        
        return toResponse(providerConfig);
    }
    
    /**
     * Delete provider configuration
     */
    @Transactional
    public void deleteProvider(String providerId) {
        log.info("Deleting provider config: providerId={}", providerId);
        
        ProviderConfig providerConfig = providerConfigRepository.findByProviderId(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + providerId));
        
        providerConfigRepository.delete(providerConfig);
        log.info("Provider config deleted: providerId={}", providerId);
    }
    
    /**
     * List all providers
     */
    @Transactional(readOnly = true)
    public List<ProviderConfigResponse> listProviders() {
        log.debug("Listing all providers");
        
        return providerConfigRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * List active providers only
     */
    @Transactional(readOnly = true)
    public List<ProviderConfigResponse> listActiveProviders() {
        log.debug("Listing active providers");
        
        return providerConfigRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert entity to response DTO
     */
    private ProviderConfigResponse toResponse(ProviderConfig providerConfig) {
        return ProviderConfigResponse.builder()
                .id(providerConfig.getId())
                .providerId(providerConfig.getProviderId())
                .providerName(providerConfig.getProviderName())
                .apiBaseUrl(providerConfig.getApiBaseUrl())
                .authType(providerConfig.getAuthType().name())
                .supportsXml(providerConfig.getSupportsXml())
                .supportsJson(providerConfig.getSupportsJson())
                .timeoutMs(providerConfig.getTimeoutMs())
                .retryAttempts(providerConfig.getRetryAttempts())
                .isActive(providerConfig.getIsActive())
                .createdAt(providerConfig.getCreatedAt())
                .updatedAt(providerConfig.getUpdatedAt())
                .build();
    }
}
