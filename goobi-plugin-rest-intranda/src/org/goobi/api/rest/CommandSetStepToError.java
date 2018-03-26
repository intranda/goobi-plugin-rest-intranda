package org.goobi.api.rest;

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
