package org.goobi.api.rest;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.goobi.api.rest.response.DeletionResponse;
import org.goobi.beans.Process;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.persistence.managers.ProcessManager;

@Path("/process")

public class CommandProcessDelete {

    @Context
    UriInfo uriInfo;

    @Path("delete/id/{processId}")
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response deleteProcess(@PathParam("processId") int processId) {
        DeletionResponse response = new DeletionResponse();
        response.setProcessId(processId);

        Process process = ProcessManager.getProcessById(processId);
        if (process == null) {
            response.setResult("error");
            response.setErrorText("Process does not exist.");
            Response resp = Response.status(Response.Status.NOT_FOUND).entity(response).build();
            return resp;

        } else {
            response.setProcessName(process.getTitel());

            deleteDirectory(process);
            ProcessManager.deleteProcess(process);
            response.setResult("success");
        }
        Response resp = Response.status(Response.Status.OK).entity(response).build();
        return resp;
    }

    private void deleteDirectory(Process process) {

        try {
            StorageProvider.getInstance().deleteDir(Paths.get(process.getProcessDataDirectory()));
            java.nio.file.Path ocr = Paths.get(process.getOcrDirectory());
            if (Files.exists(ocr)) {
                StorageProvider.getInstance().deleteDir(ocr);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
    }

}
