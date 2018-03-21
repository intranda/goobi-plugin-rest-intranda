package org.goobi.api.rest;

import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.goobi.beans.Step;

import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;

@Path("seterrorstep")
public class CommandSetStepToError {

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
}
