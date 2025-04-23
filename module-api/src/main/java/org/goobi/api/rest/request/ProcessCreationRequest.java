package org.goobi.api.rest.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessCreationRequest {

    private String identifier;
    private String processtitle;
    private String anchorDSType;
    private String logicalDSType;
    private Integer templateId;
    private String templateName;

    private OpacConfig opacConfig;

    @XmlElementWrapper
    @XmlElement(name = "metadata")
    private Map<String, String> metadata;

    @XmlElementWrapper
    @XmlElement(name = "anchorMetadata")
    private Map<String, String> anchorMetadata;

    @XmlElementWrapper
    @XmlElement(name = "property")
    @JsonProperty("properties")
    private Map<String, String> properties;

    /*
    
    // sample with opac request:
    
    {
        "processtitle": "991172064531605501_2025_07",
        "templateName": "ZHB_Periodika_PDF_API_Import",
        "opacConfig" : { "opacName": "SLSP Network", "searchField": "rec.id"},
        "identifier": "991172064531605501",
        "metadata": {"CatalogIDDigital": "991172064531605501_2025_07", "CurrentNo": "7", "CurrentNoSorting": "7"},
        "anchorMetadata": {"DocLanguage": "ger"}
    }
    
    // sample without opac request:
    
    {
        "processtitle": "991172064531605501_2025_08",
        "templateName": "ZHB_Periodika_PDF_API_Import",
        "anchorDSType": "Periodical",
        "logicalDSType": "PeriodicalVolume",
        "identifier": "991172064531605501_2025_08",
        "metadata": {"TitleDocMain": "Band 2025",
                      "PublicationYear": "2025",
                      "CurrentNo": "8",
                      "CurrentNoSorting": "8"
                     },
        "anchorMetadata": {"CatalogIDDigital": "991172064531605501",
                           "TitleDocMain": "Pfarreiblatt",
                           "DocLanguage": "ger"}
    }
    
     */

}
