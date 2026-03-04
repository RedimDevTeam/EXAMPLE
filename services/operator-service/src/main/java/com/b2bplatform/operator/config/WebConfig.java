package com.b2bplatform.operator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Suppress favicon.ico 404 errors
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
        
        // Serve uploaded branding files
        registry.addResourceHandler("/uploads/branding/**")
                .addResourceLocations("file:uploads/branding/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
