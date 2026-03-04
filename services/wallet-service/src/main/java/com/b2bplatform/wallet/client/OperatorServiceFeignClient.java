package com.b2bplatform.wallet.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client for Operator Service via API Gateway (service → api-gateway → target service).
 * Synchronous only. When gateway.url is set, uses that; otherwise Eureka resolves api-gateway.
 */
@FeignClient(name = "api-gateway", url = "${gateway.url:}", contextId = "operatorServiceFeignClient")
public interface OperatorServiceFeignClient {

    @GetMapping("/internal/api/v1/operators/{id}")
    Map<String, Object> getOperator(@PathVariable("id") Long id);

    @GetMapping("/internal/api/v1/operators/code/{code}")
    Map<String, Object> getOperatorByCode(@PathVariable("code") String code);
}
