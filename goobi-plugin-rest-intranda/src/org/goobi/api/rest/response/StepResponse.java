package org.goobi.api.rest.response;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@XmlRootElement

@JsonPropertyOrder({ "title", "status", "user", "startDate", "endDate", "status", "order" })
public @Data class StepResponse {

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssZ", timezone="CET")
    private Date startDate;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssZ", timezone="CET")
    private Date endDate;

    private String user;

    private String status;

    private String title;

    private int order;

}
