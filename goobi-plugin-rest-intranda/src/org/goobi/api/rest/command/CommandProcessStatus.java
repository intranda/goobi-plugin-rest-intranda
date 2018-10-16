package org.goobi.api.rest.command;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.goobi.api.rest.response.ProcessStatusResponse;
import org.goobi.api.rest.response.StepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.extern.log4j.Log4j;

@Path("/process")
@Log4j
public class CommandProcessStatus {

    @Context
    UriInfo uriInfo;

    @Path("details/title/{processTitle}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    //    @Produces({MediaType.APPLICATION_JSON, "application/xml"})

    public ProcessStatusResponse getProcessStatusAsJson(@PathParam("processTitle") String processTitle) {
        ProcessStatusResponse resp = getData(processTitle);
        List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(resp.getId());
        resp.setProperties(pps);
        return resp;
    }

    @Path("details/id/{processId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ProcessStatusResponse getProcessStatusAsJson(@PathParam("processId") int processId) {
        ProcessStatusResponse resp = getData(processId);
        List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processId);
        resp.setProperties(pps);
        return resp;
    }

    //    @Path("details/title/xml/{processTitle}")
    //    @GET
    //    @Produces(MediaType.TEXT_XML)
    //    public ProcessStatusResponse getProcessStatusAsXml(@PathParam("processTitle") String processTitle) {
    //        ProcessStatusResponse resp = getData(processTitle);
    //        return resp;
    //    }
    //    
    //    @Path("details/id/xml/{processId}")
    //    @GET
    //    @Produces(MediaType.TEXT_XML)
    //    public ProcessStatusResponse getProcessStatusAsXml(@PathParam("processTitle") int processId) {
    //        ProcessStatusResponse resp = getData(processId);
    //        return resp;
    //    }

    @Path("report/{startdate}/{enddate}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProcessStatusResponse> getProcessStatusList(@PathParam("startdate") String start, @PathParam("enddate") String end) {
        String sql = "IstTemplate = false AND ";

        if (end != null) {
            sql += "(erstellungsdatum BETWEEN '" + start + "' AND '" + end + "')";
        } else {
            sql += "erstellungsdatum > '" + start + "'";
        }
        List<Integer> processIdList = ProcessManager.getIDList(sql);

        List<ProcessStatusResponse> processList = new LinkedList<>();

        for (Integer processid : processIdList) {
            Process p = ProcessManager.getProcessById(processid);
            ProcessStatusResponse resp = getData(p.getTitel());
            List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processid);
            resp.setProperties(pps);
            processList.add(resp);
        }

        return processList;
    }

    @Path("report/{startdate}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProcessStatusResponse> getProcessStatusList(@PathParam("startdate") String start) {
        return getProcessStatusList(start, null);
    }

    /**
     * 
     * @param processid process id
     * @return status code 200 if process exists and has images, 206 if process exists, but has no images, 404 if process does not exist and 500 on
     *         internal error
     */
    @Path("check/id/{processId}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatusOfProcess(@PathParam("processId") int processId) {
        Process p = ProcessManager.getProcessById(processId);
        if (p == null) {
            return Response.status(Status.PARTIAL_CONTENT).entity("Process not found").build();
        }
        return checkStatusProcessContent(p);
    }

    @Path("check/title/{processTitle}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatusOfProcess(@PathParam("processTitle") String processTitle) {
        Process p = ProcessManager.getProcessByTitle(processTitle);
        if (p == null) {
            return Response.status(Status.PARTIAL_CONTENT).entity("Process not found").build();
        }
        return checkStatusProcessContent(p);
    }

    private Response checkStatusProcessContent(Process p) {
        try {
            String imageFolder = p.getImagesTifDirectory(false);
            List<String> files = StorageProvider.getInstance().list(imageFolder);
            if (files.isEmpty()) {
                return Response.status(Status.PARTIAL_CONTENT).entity("Process has no images.").build();
            }
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error.").build();
        }

        return Response.ok().entity("Process ok.").build();
    }

    private ProcessStatusResponse getData(String processTitle) {
        Process p = ProcessManager.getProcessByTitle(processTitle);
        ProcessStatusResponse resp = new ProcessStatusResponse();
        if (p == null) {
            resp.setResult("No proccess with title " + processTitle + " found");
        } else {
            createResponse(p, resp);
        }
        return resp;
    }

    private ProcessStatusResponse getData(int processId) {
        Process p = ProcessManager.getProcessById(processId);
        ProcessStatusResponse resp = new ProcessStatusResponse();
        if (p == null) {
            resp.setResult("No proccess with ID " + processId + " found");
        } else {
            createResponse(p, resp);
        }
        return resp;
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
