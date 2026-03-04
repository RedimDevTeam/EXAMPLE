package com.b2bplatform.b2c.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Service for generating and validating HMAC-SHA256 signatures
 * Used for authenticating requests to B2C providers
 */
@Service
@Slf4j
public class SignatureService {
    
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    /**
     * Generate HMAC-SHA256 signature
     * 
     * @param payload The payload to sign (typically JSON string)
     * @param secret The secret key for HMAC
     * @return Base64-encoded HMAC signature
     */
    public String generateSignature(String payload, String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM
            );
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hash);
            
            log.debug("Generated HMAC signature for payload length: {}", payload.length());
            return signature;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("HMAC-SHA256 algorithm not available", e);
            throw new RuntimeException("Signature algorithm not available", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid secret key for HMAC", e);
            throw new IllegalArgumentException("Invalid secret key", e);
        }
    }
    
    /**
     * Validate HMAC-SHA256 signature
     * 
     * @param payload The payload that was signed
     * @param secret The secret key for HMAC
     * @param providedSignature The signature to validate
     * @return true if signature is valid, false otherwise
     */
    public boolean validateSignature(String payload, String secret, String providedSignature) {
        if (providedSignature == null || providedSignature.isBlank()) {
            log.warn("Provided signature is null or empty");
            return false;
        }
        
        try {
            String expectedSignature = generateSignature(payload, secret);
            boolean isValid = expectedSignature.equals(providedSignature);
            
            if (!isValid) {
                log.warn("Signature validation failed. Expected: {}, Provided: {}", 
                    expectedSignature.substring(0, Math.min(10, expectedSignature.length())),
                    providedSignature.substring(0, Math.min(10, providedSignature.length())));
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }
    
    /**
     * Generate signature for JSON payload
     * 
     * @param jsonPayload JSON string payload
     * @param secret Secret key
     * @return HMAC signature
     */
    public String generateJsonSignature(String jsonPayload, String secret) {
        return generateSignature(jsonPayload, secret);
    }
    
    /**
     * Generate signature for XML payload
     * 
     * @param xmlPayload XML string payload
     * @param secret Secret key
     * @return HMAC signature
     */
    public String generateXmlSignature(String xmlPayload, String secret) {
        return generateSignature(xmlPayload, secret);
    }
}
