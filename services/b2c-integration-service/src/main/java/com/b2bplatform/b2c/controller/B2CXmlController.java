package com.b2bplatform.b2c.controller;

import com.b2bplatform.b2c.dto.request.*;
import com.b2bplatform.b2c.dto.response.ProviderBalanceResponse;
import com.b2bplatform.b2c.dto.response.ProviderWalletResponse;
import com.b2bplatform.b2c.model.ProviderConfig;
import com.b2bplatform.b2c.service.B2CProviderWalletService;
import com.b2bplatform.b2c.service.B2CXmlAdapterService;
import com.b2bplatform.b2c.service.ProviderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for B2C Provider Wallet Operations (XML Envelope Format)
 * 
 * Base Path: /api/v1/b2c/xml/wallet
 * Handles legacy providers that only support XML envelope format
 */
@RestController
@RequestMapping("/api/v1/b2c/xml/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "B2C Provider Wallet (XML)", description = "B2C provider wallet operations using XML envelope format")
public class B2CXmlController {
    
    private final B2CProviderWalletService walletService;
    private final B2CXmlAdapterService xmlAdapterService;
    private final ProviderConfigService providerConfigService;
    
    /**
     * Debit player wallet (XML)
     */
    @PostMapping(value = "/debit", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Debit player wallet (XML)", 
               description = "Debit player wallet via B2C provider API using XML envelope format. Provider ID extracted from XML header.")
    public ResponseEntity<String> debit(@RequestBody String xmlPayload) {
        try {
            log.info("POST /api/v1/b2c/xml/wallet/debit - Processing XML debit request");
            
            // Parse XML to extract provider ID from header
            com.b2bplatform.b2c.model.xml.XmlEnvelope envelope = 
                new com.fasterxml.jackson.dataformat.xml.XmlMapper().readValue(xmlPayload, 
                    com.b2bplatform.b2c.model.xml.XmlEnvelope.class);
            
            String providerId = envelope.getHeader().getAgentId();
            ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
            
            if (!providerConfig.getSupportsXml()) {
                throw new IllegalArgumentException("Provider does not support XML format: " + providerId);
            }
            
            // Convert XML to JSON DTO
            ProviderDebitRequest request = xmlAdapterService.xmlToDebitRequest(xmlPayload, providerConfig);
            
            // Call wallet service
            ProviderWalletResponse response = walletService.debit(providerId, request);
            
            // Convert response to XML
            String xmlResponse = xmlAdapterService.jsonToXmlResponse(response);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing XML debit request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<result><returnset><status>5000</status><message>Error: " + e.getMessage() + "</message></returnset></result>");
        }
    }
    
    /**
     * Credit player wallet (XML)
     */
    @PostMapping(value = "/credit", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Credit player wallet (XML)", 
               description = "Credit player wallet via B2C provider API using XML envelope format.")
    public ResponseEntity<String> credit(@RequestBody String xmlPayload) {
        try {
            log.info("POST /api/v1/b2c/xml/wallet/credit - Processing XML credit request");
            
            com.b2bplatform.b2c.model.xml.XmlEnvelope envelope = 
                new com.fasterxml.jackson.dataformat.xml.XmlMapper().readValue(xmlPayload, 
                    com.b2bplatform.b2c.model.xml.XmlEnvelope.class);
            
            String providerId = envelope.getHeader().getAgentId();
            ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
            
            if (!providerConfig.getSupportsXml()) {
                throw new IllegalArgumentException("Provider does not support XML format: " + providerId);
            }
            
            ProviderCreditRequest request = xmlAdapterService.xmlToCreditRequest(xmlPayload, providerConfig);
            ProviderWalletResponse response = walletService.credit(providerId, request);
            String xmlResponse = xmlAdapterService.jsonToXmlResponse(response);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing XML credit request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<result><returnset><status>5000</status><message>Error: " + e.getMessage() + "</message></returnset></result>");
        }
    }
    
    /**
     * Refund transaction (XML)
     */
    @PostMapping(value = "/refund", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Refund transaction (XML)", 
               description = "Refund a previous transaction via B2C provider API using XML envelope format.")
    public ResponseEntity<String> refund(@RequestBody String xmlPayload) {
        try {
            log.info("POST /api/v1/b2c/xml/wallet/refund - Processing XML refund request");
            
            com.b2bplatform.b2c.model.xml.XmlEnvelope envelope = 
                new com.fasterxml.jackson.dataformat.xml.XmlMapper().readValue(xmlPayload, 
                    com.b2bplatform.b2c.model.xml.XmlEnvelope.class);
            
            String providerId = envelope.getHeader().getAgentId();
            ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
            
            if (!providerConfig.getSupportsXml()) {
                throw new IllegalArgumentException("Provider does not support XML format: " + providerId);
            }
            
            ProviderRefundRequest request = xmlAdapterService.xmlToRefundRequest(xmlPayload, providerConfig);
            ProviderWalletResponse response = walletService.refund(providerId, request);
            String xmlResponse = xmlAdapterService.jsonToXmlResponse(response);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing XML refund request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<result><returnset><status>5000</status><message>Error: " + e.getMessage() + "</message></returnset></result>");
        }
    }
    
    /**
     * Cancel transaction (XML)
     */
    @PostMapping(value = "/cancel", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Cancel transaction (XML)", 
               description = "Cancel a pending transaction via B2C provider API using XML envelope format.")
    public ResponseEntity<String> cancel(@RequestBody String xmlPayload) {
        try {
            log.info("POST /api/v1/b2c/xml/wallet/cancel - Processing XML cancel request");
            
            com.b2bplatform.b2c.model.xml.XmlEnvelope envelope = 
                new com.fasterxml.jackson.dataformat.xml.XmlMapper().readValue(xmlPayload, 
                    com.b2bplatform.b2c.model.xml.XmlEnvelope.class);
            
            String providerId = envelope.getHeader().getAgentId();
            ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
            
            if (!providerConfig.getSupportsXml()) {
                throw new IllegalArgumentException("Provider does not support XML format: " + providerId);
            }
            
            String transactionId = envelope.getBody().getFundTransfer().getTransactionId();
            String playerId = envelope.getBody().getFundTransfer().getUsername();
            
            ProviderWalletResponse response = walletService.cancel(providerId, transactionId, playerId);
            String xmlResponse = xmlAdapterService.jsonToXmlResponse(response);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing XML cancel request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<result><returnset><status>5000</status><message>Error: " + e.getMessage() + "</message></returnset></result>");
        }
    }
    
    /**
     * Get player balance (XML)
     */
    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get player balance (XML)", 
               description = "Get player wallet balance via B2C provider API using XML format.")
    public ResponseEntity<String> getBalance(
            @RequestParam String providerId,
            @RequestParam String playerId,
            @RequestParam(required = false) String currency) {
        try {
            log.info("GET /api/v1/b2c/xml/wallet/balance - providerId={}, playerId={}", providerId, playerId);
            
            ProviderConfig providerConfig = providerConfigService.getProviderEntity(providerId);
            if (!providerConfig.getSupportsXml()) {
                throw new IllegalArgumentException("Provider does not support XML format: " + providerId);
            }
            
            ProviderBalanceRequest request = ProviderBalanceRequest.builder()
                    .playerId(playerId)
                    .currency(currency)
                    .build();
            
            ProviderBalanceResponse response = walletService.getBalance(providerId, request);
            String xmlResponse = xmlAdapterService.jsonToXmlBalanceResponse(response);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xmlResponse);
                    
        } catch (Exception e) {
            log.error("Error processing XML balance request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_XML)
                    .body("<result><returnset><status>5000</status><message>Error: " + e.getMessage() + "</message></returnset></result>");
        }
    }
}
