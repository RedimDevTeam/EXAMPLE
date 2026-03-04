package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateUrlConfigRequest;
import com.b2bplatform.operator.dto.request.UpdateUrlConfigRequest;
import com.b2bplatform.operator.dto.response.UrlConfigResponse;
import com.b2bplatform.operator.model.OperatorUrlConfig;
import com.b2bplatform.operator.repository.OperatorRepository;
import com.b2bplatform.operator.repository.OperatorUrlConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing operator URL configuration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorUrlConfigService {
    
    private final OperatorUrlConfigRepository urlConfigRepository;
    private final OperatorRepository operatorRepository;
    
    /**
     * Get URL config for an operator.
     */
    public Optional<UrlConfigResponse> getUrlConfigByOperatorId(Long operatorId) {
        log.debug("Fetching URL config for operator: {}", operatorId);
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found with id: " + operatorId);
        }
        
        return urlConfigRepository.findByOperatorId(operatorId)
            .map(this::toResponse);
    }
    
    /**
     * Create URL config for an operator.
     */
    @Transactional
    public UrlConfigResponse createUrlConfig(Long operatorId, CreateUrlConfigRequest request) {
        log.info("Creating URL config for operator: {}", operatorId);
        
        // Validate operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found with id: " + operatorId);
        }
        
        // Check if config already exists
        if (urlConfigRepository.existsByOperatorId(operatorId)) {
            throw new IllegalArgumentException("URL config already exists for operator: " + operatorId);
        }
        
        OperatorUrlConfig urlConfig = new OperatorUrlConfig();
        urlConfig.setOperatorId(operatorId);
        urlConfig.setRequestUrl(request.getRequestUrl());
        urlConfig.setDirectoryPath(request.getDirectoryPath());
        urlConfig.setVirtualPath(request.getVirtualPath());
        
        OperatorUrlConfig saved = urlConfigRepository.save(urlConfig);
        log.info("URL config created successfully with id: {}", saved.getId());
        return toResponse(saved);
    }
    
    /**
     * Update URL config for an operator.
     */
    @Transactional
    public UrlConfigResponse updateUrlConfig(Long operatorId, UpdateUrlConfigRequest request) {
        log.info("Updating URL config for operator: {}", operatorId);
        
        OperatorUrlConfig urlConfig = urlConfigRepository.findByOperatorId(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("URL config not found for operator: " + operatorId));
        
        if (request.getRequestUrl() != null) {
            urlConfig.setRequestUrl(request.getRequestUrl());
        }
        if (request.getDirectoryPath() != null) {
            urlConfig.setDirectoryPath(request.getDirectoryPath());
        }
        if (request.getVirtualPath() != null) {
            urlConfig.setVirtualPath(request.getVirtualPath());
        }
        
        OperatorUrlConfig updated = urlConfigRepository.save(urlConfig);
        log.info("URL config updated successfully with id: {}", updated.getId());
        return toResponse(updated);
    }
    
    /**
     * Delete URL config for an operator.
     */
    @Transactional
    public void deleteUrlConfig(Long operatorId) {
        log.info("Deleting URL config for operator: {}", operatorId);
        
        OperatorUrlConfig urlConfig = urlConfigRepository.findByOperatorId(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("URL config not found for operator: " + operatorId));
        
        urlConfigRepository.deleteById(urlConfig.getId());
        log.info("URL config deleted successfully for operator: {}", operatorId);
    }
    
    /**
     * Convert OperatorUrlConfig entity to UrlConfigResponse DTO.
     */
    private UrlConfigResponse toResponse(OperatorUrlConfig urlConfig) {
        return UrlConfigResponse.builder()
            .id(urlConfig.getId())
            .operatorId(urlConfig.getOperatorId())
            .requestUrl(urlConfig.getRequestUrl())
            .directoryPath(urlConfig.getDirectoryPath())
            .virtualPath(urlConfig.getVirtualPath())
            .createdAt(urlConfig.getCreatedAt())
            .updatedAt(urlConfig.getUpdatedAt())
            .build();
    }
}
