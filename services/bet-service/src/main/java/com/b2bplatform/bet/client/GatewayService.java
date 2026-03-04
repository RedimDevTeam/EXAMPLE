package com.b2bplatform.bet.client;

import com.b2bplatform.common.response.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for calling other services via API Gateway (service → api-gateway → target service).
 * All calls are synchronous. When gateway.url is set, uses that; otherwise Eureka resolves api-gateway.
 */
@FeignClient(name = "api-gateway", url = "${gateway.url:}", contextId = "gatewayService")
public interface GatewayService {

    @PostMapping("/internal/api/v1/wallet/debit")
    ResponseEntity<APIResponse> debit(@RequestBody Map<String, Object> request);

    @PostMapping("/internal/api/v1/wallet/credit")
    ResponseEntity<APIResponse> credit(@RequestBody Map<String, Object> request);
}
