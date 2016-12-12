package org.goobi.api.rest.request;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
public class MpiCreationRequest {

    private String user; // Benutzer
    private String barcode; // Barcode
    private String signatur; // Signatur
    private String bestand; // Bestand
    private String archiv; // Archiv
    private String titel; // Titel
    private String kommentar; // Kommentar
    private String goobiWorkflow; // Goobi Workflow
}
