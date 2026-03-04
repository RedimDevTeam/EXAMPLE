package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateIpWhitelistRequest;
import com.b2bplatform.operator.dto.request.UpdateIpWhitelistRequest;
import com.b2bplatform.operator.dto.response.IpWhitelistResponse;
import com.b2bplatform.operator.model.OperatorIpWhitelist;
import com.b2bplatform.operator.repository.OperatorIpWhitelistRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing operator IP whitelist entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorIpWhitelistService {
    
    private final OperatorIpWhitelistRepository ipWhitelistRepository;
    private final OperatorRepository operatorRepository;
    
    /**
     * Get all IP whitelist entries for an operator.
     */
    public List<IpWhitelistResponse> getIpWhitelistByOperatorId(Long operatorId) {
        log.debug("Fetching IP whitelist for operator: {}", operatorId);
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found with id: " + operatorId);
        }
        
        return ipWhitelistRepository.findByOperatorId(operatorId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active IP whitelist entries for an operator.
     */
    public List<IpWhitelistResponse> getActiveIpWhitelistByOperatorId(Long operatorId) {
        log.debug("Fetching active IP whitelist for operator: {}", operatorId);
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found with id: " + operatorId);
        }
        
        return ipWhitelistRepository.findByOperatorIdAndIsActiveTrue(operatorId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get IP whitelist entry by ID.
     */
    public Optional<IpWhitelistResponse> getIpWhitelistById(Long id) {
        log.debug("Fetching IP whitelist entry with id: {}", id);
        return ipWhitelistRepository.findById(id)
            .map(this::toResponse);
    }
    
    /**
     * Create a new IP whitelist entry.
     */
    @Transactional
    public IpWhitelistResponse createIpWhitelist(Long operatorId, CreateIpWhitelistRequest request) {
        log.info("Creating IP whitelist entry for operator: {}, IP: {}", operatorId, request.getIpAddress());
        
        // Validate operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found with id: " + operatorId);
        }
        
        // Check if IP already exists for this operator
        if (ipWhitelistRepository.existsByOperatorIdAndIpAddress(operatorId, request.getIpAddress())) {
            throw new IllegalArgumentException("IP address '" + request.getIpAddress() + "' already exists for operator: " + operatorId);
        }
        
        OperatorIpWhitelist ipWhitelist = new OperatorIpWhitelist();
        ipWhitelist.setOperatorId(operatorId);
        ipWhitelist.setIpAddress(request.getIpAddress());
        ipWhitelist.setAllowedEndpoints(request.getAllowedEndpoints() != null ? 
            request.getAllowedEndpoints().toArray(new String[0]) : null);
        ipWhitelist.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        OperatorIpWhitelist saved = ipWhitelistRepository.save(ipWhitelist);
        log.info("IP whitelist entry created successfully with id: {}", saved.getId());
        return toResponse(saved);
    }
    
    /**
     * Update an existing IP whitelist entry.
     */
    @Transactional
    public IpWhitelistResponse updateIpWhitelist(Long operatorId, Long id, UpdateIpWhitelistRequest request) {
        log.info("Updating IP whitelist entry with id: {} for operator: {}", id, operatorId);
        
        OperatorIpWhitelist ipWhitelist = ipWhitelistRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("IP whitelist entry not found with id: " + id));
        
        // Verify it belongs to the operator
        if (!ipWhitelist.getOperatorId().equals(operatorId)) {
            throw new IllegalArgumentException("IP whitelist entry does not belong to operator: " + operatorId);
        }
        
        if (request.getAllowedEndpoints() != null) {
            ipWhitelist.setAllowedEndpoints(request.getAllowedEndpoints().toArray(new String[0]));
        }
        if (request.getIsActive() != null) {
            ipWhitelist.setIsActive(request.getIsActive());
        }
        
        OperatorIpWhitelist updated = ipWhitelistRepository.save(ipWhitelist);
        log.info("IP whitelist entry updated successfully with id: {}", updated.getId());
        return toResponse(updated);
    }
    
    /**
     * Delete (deactivate) an IP whitelist entry.
     */
    @Transactional
    public void deleteIpWhitelist(Long operatorId, Long id) {
        log.info("Deleting IP whitelist entry with id: {} for operator: {}", id, operatorId);
        
        OperatorIpWhitelist ipWhitelist = ipWhitelistRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("IP whitelist entry not found with id: " + id));
        
        // Verify it belongs to the operator
        if (!ipWhitelist.getOperatorId().equals(operatorId)) {
            throw new IllegalArgumentException("IP whitelist entry does not belong to operator: " + operatorId);
        }
        
        ipWhitelistRepository.deleteById(id);
        log.info("IP whitelist entry deleted successfully with id: {}", id);
    }
    
    /**
     * Validate if an IP address is whitelisted for an operator.
     * Used by API Gateway for IP validation.
     */
    public boolean isIpWhitelisted(Long operatorId, String ipAddress, String endpoint) {
        log.debug("Validating IP: {} for operator: {}, endpoint: {}", ipAddress, operatorId, endpoint);
        
        List<OperatorIpWhitelist> whitelistEntries = 
            ipWhitelistRepository.findByOperatorIdAndIsActiveTrue(operatorId);
        
        for (OperatorIpWhitelist entry : whitelistEntries) {
            if (entry.getIpAddress().equals(ipAddress) || 
                entry.getIpAddress().equals("localhost") && ipAddress.equals("127.0.0.1")) {
                
                // If no specific endpoints configured, allow all
                if (entry.getAllowedEndpoints() == null || entry.getAllowedEndpoints().length == 0) {
                    return true;
                }
                
                // Check if endpoint matches any allowed endpoint pattern
                for (String allowedEndpoint : entry.getAllowedEndpoints()) {
                    if (endpoint.startsWith(allowedEndpoint)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Convert OperatorIpWhitelist entity to IpWhitelistResponse DTO.
     */
    private IpWhitelistResponse toResponse(OperatorIpWhitelist ipWhitelist) {
        return IpWhitelistResponse.builder()
            .id(ipWhitelist.getId())
            .operatorId(ipWhitelist.getOperatorId())
            .ipAddress(ipWhitelist.getIpAddress())
            .allowedEndpoints(ipWhitelist.getAllowedEndpoints() != null ? 
                List.of(ipWhitelist.getAllowedEndpoints()) : null)
            .isActive(ipWhitelist.getIsActive())
            .createdAt(ipWhitelist.getCreatedAt())
            .updatedAt(ipWhitelist.getUpdatedAt())
            .build();
    }
}
