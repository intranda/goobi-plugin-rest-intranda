package org.goobi.api.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Test;

import org.goobi.api.rest.request.StanfordCreationRequest;
import org.goobi.api.rest.response.CreationResponse;

public class StanfordClientTest {

    @Test
    @Ignore("This was a main method before and not a unit test, we need to migrate this if required")
    public void testSomething() {
        Client client = ClientBuilder.newClient();
        WebTarget goobiBase = client.target("http://demo03.intranda.com/goobi/api");
        WebTarget process = goobiBase.path("process");
        WebTarget creation = process.path("stanfordcreate");

        StanfordCreationRequest req = new StanfordCreationRequest();

        // used as identifier of the digital object
        req.setObjectId("123456");

        req.setObjectType("item");
        // identifier of source, used ass process title
        // must be a unique title, allowed characters: \w+
        req.setSourceID("987653");

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
        req.setGoobiWorkflow("Example_Workflow1");


        Entity<StanfordCreationRequest> ent = Entity.entity(req, MediaType.TEXT_XML);
        CreationResponse response = creation.request().header("token", "test").post(ent, CreationResponse.class);

        System.out.println(response);
    }
}

