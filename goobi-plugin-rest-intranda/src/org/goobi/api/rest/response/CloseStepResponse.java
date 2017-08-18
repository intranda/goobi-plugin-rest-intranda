package org.goobi.api.rest.response;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement
@Data
public class CloseStepResponse {
    private String result; // success, error
    private int stepId;
    private String comment;
}
