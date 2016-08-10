package org.goobi.api.rest.response;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@XmlRootElement

@JsonPropertyOrder({ "title", "status", "user", "startDate", "endDate", "status", "order" })
public @Data class StepResponse {

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date endDate;
    
    private String user;
    
    private String status;
    
    private String title;
    
    private int order;
    
}
