package org.goobi.api.rest;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.goobi.api.rest.response.CloseStepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.ShellScript;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j;

@Path("/closestep")
@Log4j
public class CommandStepClose {

    @Context
    UriInfo uriInfo;

    // TODO change to POST after itm was updated
    @Path("/{stepid}")
    @GET
    @Produces(MediaType.TEXT_XML)
    public Response closeStep(@PathParam("stepid") int stepid) {
        return closeStepAndRemoveLink(null, stepid);
    }

    @Path("/{username}/{stepid}")
    @GET
    @Produces(MediaType.TEXT_XML)
    public Response closeStepAndRemoveLink(@PathParam("username") String username, @PathParam("stepid") int stepid) {
        CloseStepResponse cr = new CloseStepResponse();
        cr.setStepId(stepid);
        String message = "success";
        Status status = Response.Status.OK;

        log.debug("closing step with id " + stepid);

        Step so = StepManager.getStepById(stepid);
        if (so == null) {
            cr.setResult("error");
            message = "Step not found";
            status = Response.Status.NOT_FOUND;
        } else {
            if (so.getValidationPlugin() != null && so.getValidationPlugin().length() > 0) {
                IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, so.getValidationPlugin());
                ivp.setStep(so);
                if (!ivp.validate()) {
                    message = "Step not closed, validation failed";
                    cr.setResult("error");
                    status = Response.Status.NOT_ACCEPTABLE;
                }
            } else {
                if (username != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(ConfigurationHelper.getInstance().getUserFolder());
                    Process po = so.getProzess();
                    sb.append(username);
                    sb.append("/");
                    sb.append(

                            po.getTitel());

                    sb.append(" [");
                    sb.append(po.getId());
                    sb.append("]");
                    String nach = sb.toString().replaceAll(" ", "__");
                    File benutzerHome = new File(nach);
                    String command = ConfigurationHelper.getInstance().getScriptDeleteSymLink() + " " + benutzerHome;

                    try {
                        ShellScript.legacyCallShell2(command, so.getProcessId());
                    } catch (java.io.IOException | InterruptedException ioe) {
                        log.error("IOException UploadFromHome", ioe);
                        message = "Removing symlink from user home failed";
                        status = Response.Status.NOT_ACCEPTABLE;
                        cr.setResult("error");
                    }
                }
            }
        }
        if (so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
            message = "Step was already closed.";
            status = Response.Status.BAD_REQUEST;
            cr.setResult("error");
        }
      

        if (status.equals(Response.Status.OK)) {
            HelperSchritte hs = new HelperSchritte();
            hs.CloseStepObjectAutomatic(so);
            log.debug("step closed");
        }
        cr.setComment(message);
        Response resp = Response.status(status).entity(cr).build();
        return resp;
    }
}
