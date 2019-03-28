package org.goobi.api.rest.response;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class ReportProblemResponse {

    private String status;
    private String errorText;
    private String errorStepName;
    private int errorStepId;
    private String destinationStepName;
    private int destinationStepId;
    private String processName;
    private int processId;

}
