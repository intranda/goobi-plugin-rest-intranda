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
import java.util.Date;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.goobi.api.rest.response.CloseStepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@Path("seterrorstep")
public class CommandSetStepToError {

    /**
     * Sets step to error by step-id
     * 
     * @param sourceid
     * @return
     */
    @Path("/{stepid}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response setStepToError(@PathParam("stepid") int sourceid) {

        try {
            Step source = StepManager.getStepById(sourceid);
            source.setBearbeitungsstatusEnum(StepStatus.ERROR);
            source.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
            source.setBearbeitungszeitpunkt(new Date());
            source.setBearbeitungsbeginn(null);

            StepManager.saveStep(source);

        } catch (DAOException e) {
            String message = "An error occured: " + e.getMessage();
            // return new CommandResponse(500,title, message);
            return Response.serverError().entity(message).build();
        }

        return Response.ok().build();
    }

    /**
     * Sets step to error by process title and step name. Uses the first step with the step name in the process
     * 
     * @param processTitle
     * @param stepName
     * @return
     */
    @Path("/processtitles/{processtitle}/{stepname}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response setStepToErrorByName(@PathParam("processtitle") String processTitle, @PathParam("stepname") String stepName) {
        Process p = ProcessManager.getProcessByExactTitle(processTitle);
        List<Step> allSteps = StepManager.getStepsForProcess(p.getId());
        Step so = null;
        for (Step step : allSteps) {
            if (step.getTitel().equals(stepName)) {
                so = step;
                break;
            }
        }
        if (so == null) {
            CloseStepResponse cr = new CloseStepResponse();
            cr.setResult("error");
            String message = "Step not found";
            cr.setComment(message);
            Status status = Response.Status.NOT_FOUND;
            return Response.status(status).entity(cr).build();
        }
        return setStepToError(so.getId());
    }
}
