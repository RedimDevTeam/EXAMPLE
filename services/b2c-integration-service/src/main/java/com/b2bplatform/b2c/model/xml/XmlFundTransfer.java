package com.b2bplatform.b2c.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * XML Fund Transfer element for B2C provider requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlFundTransfer {
    
    @JacksonXmlProperty(localName = "username")
    private String username; // Player ID
    
    @JacksonXmlProperty(localName = "amount")
    private BigDecimal amount;
    
    @JacksonXmlProperty(localName = "currency")
    private String currency;
    
    @JacksonXmlProperty(localName = "transactionid")
    private String transactionId;
    
    @JacksonXmlProperty(localName = "type")
    private String type; // debit, credit, refund, cancel
    
    @JacksonXmlProperty(localName = "roundid")
    private String roundId;
    
    @JacksonXmlProperty(localName = "gameid")
    private String gameId;
    
    @JacksonXmlProperty(localName = "originaltransactionid")
    private String originalTransactionId; // For refunds
}
