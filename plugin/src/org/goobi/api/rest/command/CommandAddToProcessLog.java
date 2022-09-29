package org.goobi.api.rest.command;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@Path("/addtoprocesslog")
public class CommandAddToProcessLog {

    /**
     * Adds message to process log of process identified by process title
     * 
     * @param processTitle
     * @param type
     * @param value
     * @return
     */
    @POST
    @Path("/processtitles/{processtitle}/{type}")
    public Response addToLogByProcessTitle(@PathParam("processtitle") String processTitle, @PathParam("type") String type, String value) {
        Process process = ProcessManager.getProcessByExactTitle(processTitle);
        return addToLog(type, value, process);
    }



    @POST
    @Path("/steps/{stepid}/{type}")
    public Response addToLogByStepId(@PathParam("stepid") Integer id, @PathParam("type") String type, String value) {
        Process process = null;
        int processId = 0;

        Step so = StepManager.getStepById(id);
        if (so == null) {
            String message = "Could not load step with id: " + id;
            return Response.status(500).entity(message).build();
            //              return new CommandResponse(title, message);
        }
        processId = so.getProcessId();
        process = ProcessManager.getProcessById(processId);

        return addToLog(type, value, process);
    }

    @POST
    @Path("/processes/{processid}/{type}")
    public Response addToLogByProcessId(@PathParam("processid") Integer processId, @PathParam("type") String type, String value) {
        Process process = null;

        process = ProcessManager.getProcessById(processId);

        if (process == null) {
            String message = "Could not load process with id: " + processId;
            return Response.status(500).entity(message).build();
        }

        return addToLog(type, value, process);
    }

    private Response addToLog(String type, String value, Process process) {
        Helper.addMessageToProcessJournal(process.getId(), LogType.getByTitle(type), value, "webapi");

        return Response.ok().build();
    }
}
