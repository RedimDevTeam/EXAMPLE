package com.b2bplatform.operator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Redis cache configuration.
 * Optionally flushes Redis cache on startup if configured.
 */
@Configuration
@Slf4j
public class RedisCacheConfig {
    
    @Value("${redis.flush-on-startup:false}")
    private boolean flushOnStartup;
    
    /**
     * Flush Redis cache on startup if configured.
     * Set redis.flush-on-startup=true in application.yml to enable.
     */
    @Bean
    public CommandLineRunner redisCacheFlusher(RedisConnectionFactory redisConnectionFactory) {
        return args -> {
            if (flushOnStartup) {
                log.info("Flushing Redis cache on startup...");
                try {
                    redisConnectionFactory.getConnection().flushAll();
                    log.info("Redis cache flushed successfully");
                } catch (Exception e) {
                    log.warn("Failed to flush Redis cache on startup: {}", e.getMessage());
                }
            } else {
                log.debug("Redis cache flush on startup is disabled (set redis.flush-on-startup=true to enable)");
            }
        };
    }
}
