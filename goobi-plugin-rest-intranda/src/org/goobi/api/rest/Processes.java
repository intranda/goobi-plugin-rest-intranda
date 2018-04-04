package org.goobi.api.rest;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.goobi.api.db.DbHelper;
import org.goobi.api.rest.response.CreationResponse;
import org.goobi.api.rest.response.RestProcess;

@Path("/processes")
public class Processes {

    //TODO: really create process here
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CreationResponse createProcess() {
        System.out.println("create process");
        CreationResponse resp = new CreationResponse();
        resp.setProcessId(123456);
        resp.setProcessName("blafasel");
        return resp;
    }

    @GET
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestProcess> simpleSearch(@QueryParam("field") String field, @QueryParam("value") String value, @QueryParam("limit") int limit,
            @QueryParam("offset") int offset) throws SQLException {

        return DbHelper.searchProcesses();
    }
}
