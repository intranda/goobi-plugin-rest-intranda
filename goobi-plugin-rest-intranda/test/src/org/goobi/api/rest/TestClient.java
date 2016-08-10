package org.goobi.api.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.goobi.api.rest.request.CreationRequest;
import org.goobi.api.rest.response.CreationResponse;

public class TestClient {

    
    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();
        WebTarget goobiBase = client.target("http://localhost:8080/Goobi/api");
        WebTarget creation = goobiBase.path("creation");
        
        
        CreationRequest req = new CreationRequest();
        req.setAll_pages(true);
        req.setBtw_number(123);
        req.setClient_instructions("instructions");
        req.setEmail("john@doe.com");
        req.setIdentifier("003192975");
        req.setItem_in_order(987);
        req.setLastname("lastname");
        req.setOrder_number(1);
        req.setPage_numbers("1-7");
        req.setProcess_template(5217);
        req.setSalutation("not sure");
        req.setSignature("shelfmark");

        
        Entity<CreationRequest> ent = Entity.entity(req, MediaType.TEXT_XML);
        CreationResponse response2 = creation.request().header("token", "test").post(ent, CreationResponse.class);

        System.out.println(response2);
        
    }
}
