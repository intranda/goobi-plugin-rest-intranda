package org.goobi.api.rest.command;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.goobi.api.rest.response.CreationResponse;

import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
@Path("/metadata")
@Log4j
public class CommandMetadataSearch {

    @Context
    UriInfo uriInfo;

    // search in all fields 

    @Path("/simplesearch/{value}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAllMetadata(@PathParam("value") String value) {
        return searchAllMetadata(null, value, null, null);
    }

    // search in specific field(s), fields are cascaded with | symbol

    @Path("/simplesearch/{field}/{value}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAllMetadata(@PathParam("field") String field, @PathParam("value") String value) {
        return searchAllMetadata(field, value, null, null);
    }

    @Path("/simplesearch/{field}/{value}/{limit}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAllMetadata(@PathParam("field") String field, @PathParam("value") String value, @PathParam("limit") Integer limit) {
        return searchAllMetadata(field, value, limit, null);
    }

    @Path("/simplesearch/{field}/{value}/{limit}/{offset}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchAllMetadata(@PathParam("field") String field, @PathParam("value") String value, @PathParam("limit") Integer limit,
            @PathParam("offset") Integer offset) {
        
        CreationResponse cr = new CreationResponse();
        cr.setProcessName(value);
        cr.setErrorText(field);
        Response resp = Response.status(Response.Status.OK).entity(cr).build();
        return resp;
    }

    // extended 

    @Path("/testresponse")
    @GET
    @Produces(MediaType.TEXT_XML)
    public CreationResponse helloWorld() {
        CreationResponse cr = new CreationResponse();
        cr.setProcessId(123);
        cr.setErrorText("field order_number is missing or empty");
        cr.setProcessName("34_Doe");
        cr.setResult("failure");
        return cr;
    }

}
