package org.goobi.api.rest.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.Processproperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@XmlRootElement
@JsonPropertyOrder({ "result", "title", "id", "creationDate", "step" })
public @Data class ProcessStatusResponse {

    private String result; // success, error

    private String title;

    private int id;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date creationDate;

    List<StepResponse> step = new ArrayList<>();

    List<Processproperty> properties;

}
