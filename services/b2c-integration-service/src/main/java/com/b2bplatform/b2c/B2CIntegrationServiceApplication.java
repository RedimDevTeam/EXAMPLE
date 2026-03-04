package com.b2bplatform.b2c;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * B2C Integration Service Application
 * 
 * Handles B2C Provider Wallet Model integration for providers who manage their own wallets.
 * Supports both JSON and XML envelope formats for legacy providers.
 */
@SpringBootApplication
public class B2CIntegrationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(B2CIntegrationServiceApplication.class, args);
    }
}
