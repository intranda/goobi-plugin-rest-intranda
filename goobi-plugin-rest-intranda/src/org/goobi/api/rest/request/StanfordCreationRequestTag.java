package org.goobi.api.rest.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.NONE)
public class StanfordCreationRequestTag {

    @XmlAttribute(name="name")
    private String name;
//    @XmlElement
    @XmlAttribute(name="value")
    private String value;
   
}
