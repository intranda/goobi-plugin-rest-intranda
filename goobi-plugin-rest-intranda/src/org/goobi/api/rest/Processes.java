package org.goobi.api.rest;

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
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.AddProcessMetadataReq;
import org.goobi.api.rest.request.DeleteProcessMetadataReq;
import org.goobi.api.rest.request.ProcessCreationRequest;
import org.goobi.api.rest.request.SearchGroup;
import org.goobi.api.rest.request.SearchQuery;
import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.api.rest.response.CreationResponse;
import org.goobi.api.rest.response.ProcessStatusResponse;
import org.goobi.api.rest.response.StepResponse;
import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@Path("/processes")
public class Processes {

    // TODO: really create process here
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public CreationResponse createProcess(ProcessCreationRequest req) {
        System.out.println("create process");

        System.out.println(req.getDocstruct());

        //        for ()




        CreationResponse resp = new CreationResponse();
        resp.setProcessId(123456);
        resp.setProcessName("blafasel");
        return resp;
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestProcess> simpleSearch(@QueryParam("field") String field, @QueryParam("value") String value, @QueryParam("limit") int limit,
            @QueryParam("offset") int offset, @QueryParam("orderby") String sortField, @QueryParam("descending") boolean sortDescending,
            @QueryParam("filterProjects") String filterProjects) throws SQLException {
        SearchQuery query = new SearchQuery(field, value, RelationalOperator.LIKE);
        SearchGroup group = new SearchGroup();
        group.addFilter(query);
        SearchRequest req = new SearchRequest();
        req.addSearchGroup(group);
        req.setLimit(limit);
        req.setOffset(offset);
        req.setSortField(sortField);
        req.setSortDescending(sortDescending);
        if (filterProjects != null) {
            req.setFilterProjects(Arrays.asList(filterProjects.split(",")));
        }

        return req.search();
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestProcess> advancedSearch(SearchRequest sr) throws SQLException {
        return sr.search();
    }

    @DELETE
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse deleteMetadata(@PathParam("id") int processId, DeleteProcessMetadataReq req)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Process p = ProcessManager.getProcessById(processId);
        return req.apply(p);
    }

    @POST
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse addMetadata(@PathParam("id") int processId, AddProcessMetadataReq req)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Process p = ProcessManager.getProcessById(processId);
        return req.apply(p);
    }

    @PUT
    @Path("/{id}/properties/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProcessProperty(@PathParam("id") int processId, @PathParam("name") String name, String newValue) {
        List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processId);
        Processproperty pp = null;
        for (Processproperty p : pps) {
            if (p.getTitel().equals(name)) {
                pp = p;
                break;
            }
        }
        if (pp == null) {
            pp = new Processproperty();
            Process p = ProcessManager.getProcessById(processId);
            pp.setProzess(p);
            pp.setTitel(name);
        }
        pp.setWert(newValue);
        PropertyManager.saveProcessProperty(pp);
        return Response.accepted().build();
    }

    @Path("/ppns/{ppn}/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProcessStatusForPPNAsJson(@PathParam("ppn") String ppn) {
        Response response = null;

        List<Integer> ids = null;
        try {
            ids = RestDbHelper.getProcessIdsForIdentifier(ppn);
        } catch (SQLException e) {

        }
        ProcessStatusResponse processStatusResponse = new ProcessStatusResponse();

        if (ids == null || ids.isEmpty()) {
            processStatusResponse.setResult("No proccess with identifier " + ppn + " found");
            response = Response.status(Response.Status.NOT_FOUND).entity(processStatusResponse).build();
        } else if (ids.size() > 1) {
            // TODO
            processStatusResponse.setResult("Found more than one process with identifier " + ppn);
            response = Response.status(Response.Status.CONFLICT).entity(processStatusResponse).build();
        } else {
            Process process = ProcessManager.getProcessById(ids.get(0));

            createResponse(process, processStatusResponse);
            response = Response.status(Response.Status.OK).entity(processStatusResponse).build();
        }

        return response;
    }

    private void createResponse(Process p, ProcessStatusResponse resp) {
        resp.setResult("ok");
        resp.setCreationDate(p.getErstellungsdatum());
        resp.setId(p.getId());
        resp.setTitle(p.getTitel());

        for (Step step : p.getSchritte()) {
            StepResponse sr = new StepResponse();
            resp.getStep().add(sr);
            sr.setEndDate(step.getBearbeitungsende());
            sr.setStartDate(step.getBearbeitungsbeginn());
            sr.setStatus(step.getBearbeitungsstatusEnum().getTitle());
            if (step.getBearbeitungsbenutzer() != null) {
                sr.setUser(step.getBearbeitungsbenutzer().getNachVorname());
            }
            sr.setTitle(step.getTitel());
            sr.setOrder(step.getReihenfolge());
        }
    }

}
