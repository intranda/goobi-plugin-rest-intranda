package org.goobi.api.rest;

import java.io.IOException;
import java.util.ArrayList;
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
import org.goobi.beans.Step;

import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Path("/process")
@Log4j
public class CommandProcessStatus {

    @Context
    UriInfo uriInfo;

    @Path("details/json/{processid}")
    @GET
    @Produces("text/json")
    public ProcessStatusResponse getProcessStatusAsJson(@PathParam("processid") int processid) {
        ProcessStatusResponse resp = getData(processid);
        return resp;
    }

    @Path("details/xml/{processid}")
    @GET
    @Produces(MediaType.TEXT_XML)
    public ProcessStatusResponse getProcessStatusAsXml(@PathParam("processid") int processid) {
        ProcessStatusResponse resp = getData(processid);
        return resp;
    }

    @Path("report/{startdate}/{enddate}")
    @GET
    @Produces("text/json")
    public List<ProcessStatusResponse> getProcessStatusList(@PathParam("startdate") String start, @PathParam("enddate") String end) {
        String sql;

        if (end != null) {
            sql = "(erstellungsdatum BETWEEN '" + start + "' AND '" + end + "')";
        } else {
            sql = "erstellungsdatum > '" + start + "'";
        }
        List<Integer> processIdList = ProcessManager.getIDList(sql);

        List<ProcessStatusResponse> processList = new LinkedList<>();

        for (Integer processid : processIdList) {
            ProcessStatusResponse resp = getData(processid);
            processList.add(resp);
        }

        return processList;
    }

    @Path("report/{startdate}")
    @GET
    @Produces("text/json")
    public List<ProcessStatusResponse> getProcessStatusList(@PathParam("startdate") String start) {
        return getProcessStatusList(start, null);
    }

    /**
     * 
     * @param processid process id
     * @return status code 200 if process exists and has images, 206 if process exists, but has no images, 404 if process does not exist and 500 on
     *         internal error
     */

    @Path("check/{processid}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatusOfProcess(@PathParam("processid") int processid) {
        Process p = ProcessManager.getProcessById(processid);
        if (p == null) {
            return Response.status(Status.BAD_REQUEST).entity("Process not found").build();
        }
        try {
            String imageFolder = p.getImagesTifDirectory(false);
            List<String> files = NIOFileUtils.list(imageFolder);
            if (files.isEmpty()) {
                return Response.status(Status.PARTIAL_CONTENT).entity("Process has no images.").build();
            }
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error.").build();
        }

        return Response.ok().entity("Process ok.").build();
    }

    private ProcessStatusResponse getData(int processid) {
        Process p = ProcessManager.getProcessById(processid);
        ProcessStatusResponse resp = new ProcessStatusResponse();
        if (p == null) {
            resp.setResult("No proccess with id " + processid + " found");
        } else {
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
        return resp;
    }

}
