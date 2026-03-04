package com.b2bplatform.b2c.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * XML Return Set for B2C provider responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlReturnSet {
    
    @JacksonXmlProperty(localName = "status")
    private Integer status; // Unified error code (1000 = Success)
    
    @JacksonXmlProperty(localName = "balance")
    private BigDecimal balance;
    
    @JacksonXmlProperty(localName = "currency")
    private String currency;
    
    @JacksonXmlProperty(localName = "unittype")
    private String unitType; // CENTS or DECIMAL
    
    @JacksonXmlProperty(localName = "message")
    private String message;
    
    @JacksonXmlProperty(localName = "transactionid")
    private String transactionId;
}
