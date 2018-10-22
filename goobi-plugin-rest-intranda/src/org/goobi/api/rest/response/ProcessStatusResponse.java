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
@JsonPropertyOrder({ "result", "title", "id", "creationDate", "processCompleted", "step" })
public @Data class ProcessStatusResponse {

    private String result; // success, error

    private String title;

    private int id;

    private boolean processCompleted;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssZ", timezone="CET")
    private Date creationDate;

    List<StepResponse> step = new ArrayList<>();

    List<PropertyResponse> properties = new ArrayList<>();

    public void addProperties(List<Processproperty> propertyList) {
        for (Processproperty property : propertyList) {
            PropertyResponse resp = new PropertyResponse();
            resp.setTitle(property.getTitel());
            resp.setValue(property.getWert());
            properties.add(resp);
        }
    }

}
