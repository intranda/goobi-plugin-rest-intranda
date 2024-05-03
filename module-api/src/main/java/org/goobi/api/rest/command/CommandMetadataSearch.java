package org.goobi.api.rest.command;

/**
 * This file is part of a plugin for the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
