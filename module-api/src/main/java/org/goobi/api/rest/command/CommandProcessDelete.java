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
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

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
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();

        } else {
            response.setProcessName(process.getTitel());

            deleteDirectory(process);
            ProcessManager.deleteProcess(process);
            response.setResult("success");
        }
        return Response.status(Response.Status.OK).entity(response).build();
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
