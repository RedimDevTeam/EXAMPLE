package com.b2bplatform.operator.service;

import com.b2bplatform.operator.model.Operator;
import com.b2bplatform.operator.model.OperatorApiKey;
import com.b2bplatform.operator.repository.OperatorApiKeyRepository;
import com.b2bplatform.operator.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorApiKeyService {
    
    private final OperatorApiKeyRepository apiKeyRepository;
    private final OperatorRepository operatorRepository;
    
    /**
     * Validate API key and return operator ID if valid
     */
    @Transactional
    public Optional<Long> validateApiKey(String apiKey) {
        log.debug("Validating API key: {}", maskApiKey(apiKey));
        
        Optional<OperatorApiKey> keyOpt = apiKeyRepository.findByApiKey(apiKey);
        
        if (keyOpt.isEmpty()) {
            log.warn("API key not found: {}", maskApiKey(apiKey));
            return Optional.empty();
        }
        
        OperatorApiKey apiKeyEntity = keyOpt.get();
        
        // Check if key is active
        if (!apiKeyEntity.isActive()) {
            log.warn("API key is not active: {}", maskApiKey(apiKey));
            return Optional.empty();
        }
        
        // Update last used timestamp
        apiKeyRepository.updateLastUsedAt(apiKeyEntity.getId(), LocalDateTime.now());
        
        log.debug("API key validated successfully for operator: {}", apiKeyEntity.getOperatorId());
        return Optional.of(apiKeyEntity.getOperatorId());
    }
    
    /**
     * Create a new API key for an operator
     */
    @Transactional
    public OperatorApiKey createApiKey(Long operatorId, String keyName) {
        log.info("Creating API key for operator: {}", operatorId);
        
        // Verify operator exists
        Operator operator = operatorRepository.findById(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found: " + operatorId));
        
        OperatorApiKey apiKey = new OperatorApiKey();
        apiKey.setOperatorId(operatorId);
        apiKey.setKeyName(keyName);
        apiKey.setStatus("ACTIVE");
        
        OperatorApiKey saved = apiKeyRepository.save(apiKey);
        log.info("API key created: {} for operator: {}", maskApiKey(saved.getApiKey()), operatorId);
        
        return saved;
    }
    
    /**
     * Get all API keys for an operator
     */
    public List<OperatorApiKey> getApiKeysByOperator(Long operatorId) {
        return apiKeyRepository.findAll().stream()
            .filter(key -> key.getOperatorId().equals(operatorId))
            .toList();
    }
    
    /**
     * Revoke an API key
     */
    @Transactional
    public void revokeApiKey(String apiKey) {
        log.info("Revoking API key: {}", maskApiKey(apiKey));
        
        Optional<OperatorApiKey> keyOpt = apiKeyRepository.findByApiKey(apiKey);
        if (keyOpt.isPresent()) {
            OperatorApiKey apiKeyEntity = keyOpt.get();
            apiKeyEntity.setStatus("REVOKED");
            apiKeyRepository.save(apiKeyEntity);
            log.info("API key revoked: {}", maskApiKey(apiKey));
        }
    }
    
    /**
     * Activate/Deactivate an API key
     */
    @Transactional
    public void updateApiKeyStatus(String apiKey, String status) {
        log.info("Updating API key status: {} to {}", maskApiKey(apiKey), status);
        
        Optional<OperatorApiKey> keyOpt = apiKeyRepository.findByApiKey(apiKey);
        if (keyOpt.isPresent()) {
            OperatorApiKey apiKeyEntity = keyOpt.get();
            apiKeyEntity.setStatus(status);
            apiKeyRepository.save(apiKeyEntity);
            log.info("API key status updated: {} to {}", maskApiKey(apiKey), status);
        }
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "***";
        }
        return apiKey.substring(0, 7) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
