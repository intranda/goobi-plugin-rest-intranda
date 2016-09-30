package org.goobi.api.rest.request;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class StanfordCreationRequest {

    private String objectID;
    private String sourceID;
    private String objectLabel;
    private String tag_Process;
    private String tag_ContentType;
    private String tag_Project;

}
