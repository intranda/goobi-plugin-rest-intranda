package org.goobi.api.rest.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import lombok.Data;
@Data
@XmlAccessorType(XmlAccessType.NONE)
public class ProcessCreationElement {

    @XmlAttribute(name="name")
    private String name;
    @XmlAttribute(name="value")
    private String value;
}
