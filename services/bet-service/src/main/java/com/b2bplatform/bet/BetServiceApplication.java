package com.b2bplatform.bet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BetServiceApplication.class, args);
    }
}
