package org.goobi.api.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.goobi.api.rest.request.StanfordCreationRequest;
import org.goobi.api.rest.response.CreationResponse;

public class TestClient {

    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();
        WebTarget goobiBase = client.target("http://demo03.intranda.com/goobi/api");
        WebTarget process = goobiBase.path("process");
        WebTarget creation = process.path("create");

        StanfordCreationRequest req = new StanfordCreationRequest();

        // used as identifier of the digital object
        req.setObjectID("123456");
        // used as title of the digital object
        req.setObjectLabel("Main title of the book");
        // identifier of source, used ass process title
        req.setSourceID("98765");
        // content type is hard coded in plugin
        req.setTag_ContentType("Monograph");
        // must be a unique title, allowed characters: \w+
        req.setTag_Process("some keyword for process");
        // must match workflow name in goobi instance
        req.setTag_Project("Example_workflow_LayoutWizzard_Stanford");

        Entity<StanfordCreationRequest> ent = Entity.entity(req, MediaType.TEXT_XML);
        CreationResponse response = creation.request().header("token", "test").post(ent, CreationResponse.class);

        System.out.println(response);

    }
}
