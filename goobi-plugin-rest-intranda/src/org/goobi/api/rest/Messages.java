package org.goobi.api.rest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/messages")
public class Messages {

    @GET
    @Path("/{language}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getBundleForLanguage(@PathParam("language") String language) {
        Locale locale = new Locale(language);
        ResourceBundle bundle = ResourceBundle.getBundle("messages.messages", locale);
        Map<String, String> bundleMap = new HashMap<>();
        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            bundleMap.put(key, bundle.getString(key));
        }
        return bundleMap;
    }
}
