package org.goobi.api.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
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
import org.goobi.api.rest.request.SearchGroup;
import org.goobi.api.rest.request.SearchQuery;
import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.api.rest.request.UpdateProcessMetadataReq;
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
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestProcess> simpleSearch(@QueryParam("field") String field, @QueryParam("value") String value, @QueryParam("limit") int limit,
            @QueryParam("offset") int offset, @QueryParam("orderby") String sortField, @QueryParam("descending") boolean sortDescending)
                    throws SQLException {
        SearchQuery query = new SearchQuery(field, value, RelationalOperator.LIKE);
        SearchGroup group = new SearchGroup();
        group.addFilter(query);
        SearchRequest req = new SearchRequest();
        req.addSearchGroup(group);
        req.setLimit(limit);
        req.setOffset(offset);
        req.setSortField(sortField);
        req.setSortDescending(sortDescending);

        return req.search();
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestProcess> advancedSearch(SearchRequest sr) throws SQLException {
        return sr.search();
    }

    @PUT
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse updateMetadata(@PathParam("id") int processId, UpdateProcessMetadataReq req) throws ReadException,
    PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
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
            return Response.status(404).build();
        }
        pp.setWert(newValue);
        PropertyManager.saveProcessProperty(pp);
        return Response.accepted().build();
    }

    @Path("{ppn}/status")
    @GET
    @Produces("text/json")
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
