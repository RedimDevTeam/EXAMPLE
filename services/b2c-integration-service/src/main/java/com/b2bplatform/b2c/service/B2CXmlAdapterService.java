package com.b2bplatform.b2c.service;

import com.b2bplatform.b2c.dto.request.*;
import com.b2bplatform.b2c.dto.response.ProviderBalanceResponse;
import com.b2bplatform.b2c.dto.response.ProviderWalletResponse;
import com.b2bplatform.b2c.model.ProviderConfig;
import com.b2bplatform.b2c.model.xml.*;
import com.b2bplatform.b2c.util.SignatureService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for converting between XML envelope format and JSON DTOs
 * Handles legacy providers that only support XML
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class B2CXmlAdapterService {
    
    private final XmlMapper xmlMapper;
    private final SignatureService signatureService;
    
    /**
     * Convert XML request envelope to JSON DTO for debit
     */
    public ProviderDebitRequest xmlToDebitRequest(String xmlPayload, ProviderConfig providerConfig) throws Exception {
        XmlEnvelope envelope = xmlMapper.readValue(xmlPayload, XmlEnvelope.class);
        
        // Validate signature if HMAC is used
        if (providerConfig.getAuthType() == ProviderConfig.AuthType.HMAC) {
            if (!signatureService.validateSignature(xmlPayload, providerConfig.getApiSecret(), 
                    envelope.getHeader().getSignature())) {
                throw new IllegalArgumentException("Invalid HMAC signature");
            }
        }
        
        XmlFundTransfer fundTransfer = envelope.getBody().getFundTransfer();
        
        return ProviderDebitRequest.builder()
                .playerId(fundTransfer.getUsername())
                .amount(fundTransfer.getAmount())
                .currency(fundTransfer.getCurrency())
                .unitType("CENTS") // Default, may need to be extracted from XML
                .transactionId(fundTransfer.getTransactionId())
                .transactionSubtypeId(300) // Default DEBIT
                .playerLevel(1) // Default, may need to be extracted
                .gameId(fundTransfer.getGameId())
                .roundId(fundTransfer.getRoundId())
                .build();
    }
    
    /**
     * Convert XML request envelope to JSON DTO for credit
     */
    public ProviderCreditRequest xmlToCreditRequest(String xmlPayload, ProviderConfig providerConfig) throws Exception {
        XmlEnvelope envelope = xmlMapper.readValue(xmlPayload, XmlEnvelope.class);
        
        // Validate signature
        if (providerConfig.getAuthType() == ProviderConfig.AuthType.HMAC) {
            if (!signatureService.validateSignature(xmlPayload, providerConfig.getApiSecret(), 
                    envelope.getHeader().getSignature())) {
                throw new IllegalArgumentException("Invalid HMAC signature");
            }
        }
        
        XmlFundTransfer fundTransfer = envelope.getBody().getFundTransfer();
        
        return ProviderCreditRequest.builder()
                .playerId(fundTransfer.getUsername())
                .amount(fundTransfer.getAmount())
                .currency(fundTransfer.getCurrency())
                .unitType("CENTS")
                .transactionId(fundTransfer.getTransactionId())
                .transactionSubtypeId(301) // Default CREDIT
                .playerLevel(1)
                .gameId(fundTransfer.getGameId())
                .roundId(fundTransfer.getRoundId())
                .build();
    }
    
    /**
     * Convert XML request envelope to JSON DTO for refund
     */
    public ProviderRefundRequest xmlToRefundRequest(String xmlPayload, ProviderConfig providerConfig) throws Exception {
        XmlEnvelope envelope = xmlMapper.readValue(xmlPayload, XmlEnvelope.class);
        
        // Validate signature
        if (providerConfig.getAuthType() == ProviderConfig.AuthType.HMAC) {
            if (!signatureService.validateSignature(xmlPayload, providerConfig.getApiSecret(), 
                    envelope.getHeader().getSignature())) {
                throw new IllegalArgumentException("Invalid HMAC signature");
            }
        }
        
        XmlFundTransfer fundTransfer = envelope.getBody().getFundTransfer();
        
        return ProviderRefundRequest.builder()
                .playerId(fundTransfer.getUsername())
                .originalTransactionId(fundTransfer.getOriginalTransactionId())
                .transactionId(fundTransfer.getTransactionId())
                .amount(fundTransfer.getAmount())
                .currency(fundTransfer.getCurrency())
                .unitType("CENTS")
                .transactionSubtypeId(304) // REFUND
                .roundId(fundTransfer.getRoundId())
                .gameId(fundTransfer.getGameId())
                .build();
    }
    
    /**
     * Convert JSON response to XML response envelope
     */
    public String jsonToXmlResponse(ProviderWalletResponse response) throws Exception {
        XmlReturnSet returnSet = XmlReturnSet.builder()
                .status(response.getStatus())
                .balance(response.getBalance())
                .currency(response.getCurrency())
                .unitType(response.getUnitType())
                .message(response.getMessage())
                .transactionId(response.getTransactionId())
                .build();
        
        XmlResult result = XmlResult.builder()
                .returnSet(returnSet)
                .build();
        
        return xmlMapper.writeValueAsString(result);
    }
    
    /**
     * Convert JSON balance response to XML
     */
    public String jsonToXmlBalanceResponse(ProviderBalanceResponse response) throws Exception {
        XmlReturnSet returnSet = XmlReturnSet.builder()
                .status(response.getStatus())
                .balance(response.getBalance())
                .currency(response.getCurrency())
                .unitType(response.getUnitType())
                .message(response.getMessage())
                .build();
        
        XmlResult result = XmlResult.builder()
                .returnSet(returnSet)
                .build();
        
        return xmlMapper.writeValueAsString(result);
    }
    
    /**
     * Build XML request envelope for provider API call
     */
    public String buildXmlRequest(ProviderConfig providerConfig, Object requestDto, String operation) throws Exception {
        XmlFundTransfer fundTransfer = null;
        
        if (requestDto instanceof ProviderDebitRequest) {
            ProviderDebitRequest debitRequest = (ProviderDebitRequest) requestDto;
            fundTransfer = XmlFundTransfer.builder()
                    .username(debitRequest.getPlayerId())
                    .amount(debitRequest.getAmount())
                    .currency(debitRequest.getCurrency())
                    .transactionId(debitRequest.getTransactionId())
                    .type("debit")
                    .gameId(debitRequest.getGameId())
                    .roundId(debitRequest.getRoundId())
                    .build();
        } else if (requestDto instanceof ProviderCreditRequest) {
            ProviderCreditRequest creditRequest = (ProviderCreditRequest) requestDto;
            fundTransfer = XmlFundTransfer.builder()
                    .username(creditRequest.getPlayerId())
                    .amount(creditRequest.getAmount())
                    .currency(creditRequest.getCurrency())
                    .transactionId(creditRequest.getTransactionId())
                    .type("credit")
                    .gameId(creditRequest.getGameId())
                    .roundId(creditRequest.getRoundId())
                    .build();
        } else if (requestDto instanceof ProviderRefundRequest) {
            ProviderRefundRequest refundRequest = (ProviderRefundRequest) requestDto;
            fundTransfer = XmlFundTransfer.builder()
                    .username(refundRequest.getPlayerId())
                    .amount(refundRequest.getAmount())
                    .currency(refundRequest.getCurrency())
                    .transactionId(refundRequest.getTransactionId())
                    .originalTransactionId(refundRequest.getOriginalTransactionId())
                    .type("refund")
                    .gameId(refundRequest.getGameId())
                    .roundId(refundRequest.getRoundId())
                    .build();
        }
        
        if (fundTransfer == null) {
            throw new IllegalArgumentException("Unsupported request type for XML conversion");
        }
        
        XmlBody body = XmlBody.builder()
                .fundTransfer(fundTransfer)
                .build();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String signature = null;
        
        if (providerConfig.getAuthType() == ProviderConfig.AuthType.HMAC) {
            // Generate signature (will be added after XML is built)
            String xmlWithoutSignature = xmlMapper.writeValueAsString(
                XmlEnvelope.builder()
                    .header(XmlHeader.builder()
                        .agentId(providerConfig.getProviderId())
                        .timestamp(timestamp)
                        .signature("") // Placeholder
                        .build())
                    .body(body)
                    .build()
            );
            signature = signatureService.generateXmlSignature(xmlWithoutSignature, providerConfig.getApiSecret());
        }
        
        XmlHeader header = XmlHeader.builder()
                .agentId(providerConfig.getProviderId())
                .timestamp(timestamp)
                .signature(signature)
                .build();
        
        XmlEnvelope envelope = XmlEnvelope.builder()
                .header(header)
                .body(body)
                .build();
        
        return xmlMapper.writeValueAsString(envelope);
    }
}
