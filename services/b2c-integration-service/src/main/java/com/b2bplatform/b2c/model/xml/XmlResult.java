package com.b2bplatform.b2c.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * XML Response envelope for B2C provider responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "result")
public class XmlResult {
    
    @JacksonXmlProperty(localName = "returnset")
    private XmlReturnSet returnSet;
}
