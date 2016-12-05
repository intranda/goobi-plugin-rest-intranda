package org.goobi.api.rest.response;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class DeletionResponse {

    private String result; // success, error
    
    private String errorText;

    private String processName;

    private int processId;
    
}
