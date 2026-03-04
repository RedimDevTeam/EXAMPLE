package com.b2bplatform.bet.client;

import com.b2bplatform.common.enums.StatusCode;
import com.b2bplatform.common.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Synchronous client for Wallet Service via API Gateway (service → api-gateway → target service).
 * Uses Feign GatewayService; no WebFlux/Mono.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletServiceClient {

    private final GatewayService gatewayService;

    /**
     * Debit wallet (sync). Returns map with success, transactionId, error, etc.
     * On failure returns map with success=false and error message.
     */
    public Map<String, Object> debit(Long operatorId, String playerId, BigDecimal amount,
                                    String currency, String reference, String description) {
        log.debug("Calling Wallet Service debit via gateway - operator: {}, player: {}, amount: {}",
            operatorId, playerId, amount);
        Map<String, Object> request = new HashMap<>();
        request.put("operatorId", operatorId);
        request.put("playerId", playerId);
        request.put("amount", amount);
        request.put("currency", currency != null ? currency : "USD");
        request.put("reference", reference != null ? reference : "");
        request.put("description", description != null ? description : "");
        try {
            ResponseEntity<APIResponse> resp = gatewayService.debit(request);
            if (resp.getBody() == null) {
                return Map.of("success", false, "error", "Empty response from wallet");
            }
            APIResponse api = resp.getBody();
            if (api.getCode() != StatusCode.SUCCESS.value()) {
                return Map.of("success", false, "error",
                    api.getResult() != null ? api.getResult().toString() : "Wallet debit failed");
            }
            Object result = api.getResult();
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultMap = (Map<String, Object>) result;
                resultMap.put("success", true);
                return resultMap;
            }
            return Map.of("success", true, "result", result);
        } catch (Exception e) {
            log.error("Wallet debit failed: {}", e.getMessage());
            return Map.of("success", false, "error", "Wallet Service unavailable: " + e.getMessage());
        }
    }

    /**
     * Credit wallet (sync). Returns map with success, transactionId, error, etc.
     */
    public Map<String, Object> credit(Long operatorId, String playerId, BigDecimal amount,
                                      String currency, String reference, String description) {
        log.debug("Calling Wallet Service credit via gateway - operator: {}, player: {}, amount: {}",
            operatorId, playerId, amount);
        Map<String, Object> request = new HashMap<>();
        request.put("operatorId", operatorId);
        request.put("playerId", playerId);
        request.put("amount", amount);
        request.put("currency", currency != null ? currency : "USD");
        request.put("reference", reference != null ? reference : "");
        request.put("description", description != null ? description : "");
        try {
            ResponseEntity<APIResponse> resp = gatewayService.credit(request);
            if (resp.getBody() == null) {
                return Map.of("success", false, "error", "Empty response from wallet");
            }
            APIResponse api = resp.getBody();
            if (api.getCode() != StatusCode.SUCCESS.value()) {
                return Map.of("success", false, "error",
                    api.getResult() != null ? api.getResult().toString() : "Wallet credit failed");
            }
            Object result = api.getResult();
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultMap = (Map<String, Object>) result;
                resultMap.put("success", true);
                return resultMap;
            }
            return Map.of("success", true, "result", result);
        } catch (Exception e) {
            log.error("Wallet credit failed: {}", e.getMessage());
            return Map.of("success", false, "error", "Wallet Service unavailable: " + e.getMessage());
        }
    }
}
