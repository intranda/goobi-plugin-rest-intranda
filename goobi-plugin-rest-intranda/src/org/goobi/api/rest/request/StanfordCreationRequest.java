package org.goobi.api.rest.request;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StanfordCreationRequest {

    private String objectId; // druid:xx123yy4567
    private String objectType; // item
    private String sourceID; // source:76543
    private String title; // book title
    private String contentType; // image
    private String  project; // goobi project
    private String  ocr; // ocr true or false
    private String catkey; // 31232818
    private String barcode; // 1029287645
    private String  collectionId; // druid:xx123yy4567
    private String  collectionName; // Fitch Photographs
    private String  sdrWorkflow; // dpgImageWF
    private String  goobiWorkflow; // workflow name
   
    @XmlElementWrapper
    @XmlElement(name = "tag")
    private List<StanfordCreationRequestTag> tags = new ArrayList<>();

//    @XmlElementWrapper
//    private List<StanfordCreationRequestTag> tag = new ArrayList<>();

}
