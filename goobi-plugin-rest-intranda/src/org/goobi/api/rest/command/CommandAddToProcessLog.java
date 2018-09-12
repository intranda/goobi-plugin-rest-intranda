package org.goobi.api.rest.command;

import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

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
        LogEntry logEntry = new LogEntry();
        logEntry.setContent(value);
        logEntry.setCreationDate(new Date());
        logEntry.setProcessId(process.getId());
        logEntry.setType(LogType.getByTitle(type));

        logEntry.setUserName("webapi");

        ProcessManager.saveLogEntry(logEntry);
        return Response.ok().build();
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

        LogEntry logEntry = new LogEntry();
        logEntry.setContent(value);
        logEntry.setCreationDate(new Date());
        logEntry.setProcessId(process.getId());
        logEntry.setType(LogType.getByTitle(type));

        logEntry.setUserName("webapi");

        ProcessManager.saveLogEntry(logEntry);
        return Response.ok().build();
    }

    @POST
    @Path("/processes/{processid}/{type}")
    public Response addToLogByProcessId(@PathParam("processid") Integer processId, @PathParam("type") String type, String value) {
        Process process = null;

        process = ProcessManager.getProcessById(processId);

        LogEntry logEntry = new LogEntry();
        logEntry.setContent(value);
        logEntry.setCreationDate(new Date());
        logEntry.setProcessId(process.getId());
        logEntry.setType(LogType.getByTitle(type));

        logEntry.setUserName("webapi");

        ProcessManager.saveLogEntry(logEntry);
        return Response.ok().build();
    }
}
