package com.b2bplatform.b2c.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * XML Envelope for B2C provider requests/responses
 * Legacy format for providers that don't support JSON
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "request")
public class XmlEnvelope {
    
    @JacksonXmlProperty(localName = "header")
    private XmlHeader header;
    
    @JacksonXmlProperty(localName = "body")
    private XmlBody body;
}
