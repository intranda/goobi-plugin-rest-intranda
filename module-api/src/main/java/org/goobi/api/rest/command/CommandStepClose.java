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
import java.io.File;
import java.util.List;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

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
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j;

@Path("/closestep")
@Log4j
public class CommandStepClose {

    @Context
    UriInfo uriInfo;

    /**
     * Closes step by process title and step name. Uses the first step with the step name in the process
     * 
     * @param processTitle
     * @param stepName
     * @return
     */
    @Path("/processtitles/{processtitle}/{stepname}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStepByName(@PathParam("processtitle") String processTitle, @PathParam("stepname") String stepName) {
        Process p = ProcessManager.getProcessByExactTitle(processTitle);
        return closeStep(stepName, p);
    }


    @Path("/processid/{processid}/{stepname}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStepByProcessIdAndName(@PathParam("processid") Integer processid, @PathParam("stepname") String stepName) {
        Process p = ProcessManager.getProcessById(processid);
        return closeStep(stepName, p);
    }

    private Response closeStep(String stepName, Process p) {
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
        return closeStepAndRemoveLink(null, so.getId());
    }

    @Path("/{stepid}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStep(@PathParam("stepid") int stepid) {
        return closeStepAndRemoveLink(null, stepid);
    }

    @Path("/{username}/{stepid}")
    @POST
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
        if (so != null && so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
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
        return Response.status(status).entity(cr).build();
    }
}
