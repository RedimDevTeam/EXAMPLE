package com.b2bplatform.gateway.config;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * LoadBalancer configuration so the gateway can call other services via Eureka (e.g. lb://operator-service).
 */
@Configuration
public class LoadBalancerConfig {

    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder(LoadBalancedExchangeFilterFunction loadBalancedExchangeFilterFunction) {
        return WebClient.builder()
            .filter(loadBalancedExchangeFilterFunction);
    }
}
