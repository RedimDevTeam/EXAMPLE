package com.b2bplatform.b2c.model.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * XML Body for B2C provider requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlBody {
    
    @JacksonXmlProperty(localName = "fundtransfer")
    private XmlFundTransfer fundTransfer;
}
