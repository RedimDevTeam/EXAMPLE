package com.b2bplatform.b2c.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * XML Header for B2C provider requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlHeader {
    
    @JacksonXmlProperty(localName = "agentid")
    private String agentId; // Provider ID
    
    @JacksonXmlProperty(localName = "timestamp")
    private String timestamp;
    
    @JacksonXmlProperty(localName = "signature")
    private String signature; // HMAC signature
}
