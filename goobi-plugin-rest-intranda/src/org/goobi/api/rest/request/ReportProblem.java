package org.goobi.api.rest.request;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class ReportProblem {

    private String stepId;
    private String destinationStepName;
    private String errorText;

}
