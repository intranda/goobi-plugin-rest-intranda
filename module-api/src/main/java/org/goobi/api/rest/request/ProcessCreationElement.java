package org.goobi.api.rest.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Data;
@Data
@XmlAccessorType(XmlAccessType.NONE)
public class ProcessCreationElement {

    @XmlAttribute(name="name")
    private String name;
    @XmlAttribute(name="value")
    private String value;
}
