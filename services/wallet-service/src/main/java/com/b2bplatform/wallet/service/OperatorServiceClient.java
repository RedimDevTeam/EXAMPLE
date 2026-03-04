package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.client.OperatorServiceFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Synchronous client for Operator Service via API Gateway (Feign: service → api-gateway → target service).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OperatorServiceClient {

    private final OperatorServiceFeignClient operatorServiceFeignClient;

    /** Get operator by ID (sync). Returns null on error. */
    public Map<String, Object> getOperator(Long operatorId) {
        log.debug("Getting operator: {}", operatorId);
        try {
            return operatorServiceFeignClient.getOperator(operatorId);
        } catch (Exception ex) {
            log.error("Error getting operator: {}", ex.getMessage());
            return null;
        }
    }

    /** Get operator by code (sync). Returns null on error. */
    public Map<String, Object> getOperatorByCode(String operatorCode) {
        log.debug("Getting operator by code: {}", operatorCode);
        try {
            return operatorServiceFeignClient.getOperatorByCode(operatorCode);
        } catch (Exception ex) {
            log.error("Error getting operator by code {}: {}", operatorCode, ex.getMessage());
            return null;
        }
    }

    /** Get operator ID by code (sync). Returns null if operator not found. */
    public Long getOperatorIdByCode(String operatorCode) {
        Map<String, Object> operator = getOperatorByCode(operatorCode);
        if (operator != null && operator.containsKey("id")) {
            Object idObj = operator.get("id");
            if (idObj instanceof Number) {
                return ((Number) idObj).longValue();
            }
        }
        return null;
    }
}
