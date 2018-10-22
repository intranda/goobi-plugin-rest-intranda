package org.goobi.api.rest.response;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "title", "value" })
@Data
@XmlRootElement
public class PropertyResponse {
    private String title;
    private String value;
}
