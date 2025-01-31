package org.goobi.api.rest.request;

import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessCreationRequest {

    private String identifier;
    private String processtitle;
    private String logicalDSType;
    private Integer templateId;
    private String templateName;

    private OpacConfig opacConfig;

    @XmlElementWrapper
    @XmlElement(name = "metadata")
    private Map<String, String> metadata;

    @XmlElementWrapper
    @XmlElement(name = "property")
    @JsonProperty("properties")
    private Map<String, String> properties;

    /*
    
    <?xml version="1.0"?>
    <record>
     <identifier>PPN1234567</identifier>
     <processtitle>foobar_XYZ_PPN1234567</processtitle>
     <docstruct>Monograph</docstruct>
    
    <metadataList>
     <metadata name="TitleDocMain" value="Lorem Ipsum dolor sit
    amet" />
     <metadata name="Author" value="Mustermann, Max" />
     <metadata name="PublicationYear" value="1984" />
     <metadata name="DocLanguage" value="ger" />
     <metadata name="DocLanguage" value="lat" />
     <metadata name="shelfmarksource" value="SHLF98A2" />
    </metadataList>
    
    <propertyList>
     <property name="OCR" value="Fraktur" />
    </propertyList>
    </record>
    
    
     */

}
