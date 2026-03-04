package com.b2bplatform.b2c.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for B2C Integration Service
 */
@Configuration
public class B2CConfig {
    
    /**
     * Configure ObjectMapper with JavaTimeModule for proper date/time serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    /**
     * Configure XmlMapper for XML envelope processing
     */
    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
