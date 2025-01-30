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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.goobi.api.rest.response.ProcessStatusResponse;
import org.goobi.api.rest.response.StepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;

import de.sub.goobi.helper.StorageProvider;
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
        resp.addProperties(pps);
        return resp;
    }

    @Path("details/id/{processId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ProcessStatusResponse getProcessStatusAsJson(@PathParam("processId") int processId) {
        ProcessStatusResponse resp = getData(processId);
        List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processId);
        resp.addProperties(pps);
        return resp;
    }

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
        List<Integer> processIdList = ProcessManager.getIdsForFilter(sql);

        List<ProcessStatusResponse> processList = new LinkedList<>();

        for (Integer processid : processIdList) {
            Process p = ProcessManager.getProcessById(processid);
            ProcessStatusResponse resp = getData(p.getTitel());
            List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processid);
            resp.addProperties(pps);
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
        } catch (IOException | SwapException e) {
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
        resp.setProject(p.getProjekt().getTitel());
        resp.setRuleset(p.getRegelsatz().getDatei());
        if ("100000000".equals(p.getSortHelperStatus())) {
            resp.setProcessCompleted(true);
        } else {
            resp.setProcessCompleted(false);
        }

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
            sr.setId(step.getId());
        }
    }
}
