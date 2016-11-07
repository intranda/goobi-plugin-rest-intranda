package org.goobi.api.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.goobi.api.rest.request.StanfordCreationRequest;
import org.goobi.api.rest.response.CreationResponse;

public class StanfordTestClient {

    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();
        WebTarget goobiBase = client.target("http://demo03.intranda.com/goobi/api");
        WebTarget process = goobiBase.path("process");
        WebTarget creation = process.path("stanfordcreate");

        StanfordCreationRequest req = new StanfordCreationRequest();

        // used as identifier of the digital object
        req.setObjectID("123456");

        req.setObjectType("item");
        // identifier of source, used ass process title
        // must be a unique title, allowed characters: \w+
        req.setSourceID("98765-2016-11-07");

        // used as title of the digital object
        req.setTitle("Main title of the book");
        // some tag for content type
        req.setContentType("Monograph");
        req.setProject("ABC");
        req.setCatkey("31232818");
        req.setBarcode("1029287645");
        req.setCollectionId("druid:xx123yy4567");
        req.setCollectionName("Fitch Photographs");
        req.setSdrWorkflow("dpgImageWF");

        // must match workflow name in goobi instance
        req.setGoobiWorkflow("Example_Workflow");


        Entity<StanfordCreationRequest> ent = Entity.entity(req, MediaType.TEXT_XML);
        CreationResponse response = creation.request().header("token", "test").post(ent, CreationResponse.class);

        System.out.println(response);

    }
}

