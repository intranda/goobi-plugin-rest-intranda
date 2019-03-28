package org.goobi.api.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.goobi.api.rest.request.ReportProblem;
import org.goobi.api.rest.response.ReportProblemResponse;
import org.goobi.beans.ErrorProperty;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@Path("/steps")
public class Steps {

    @Path("/{id}/reportproblem/{destinationTitle}/{errortext}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportProblemForTask(@PathParam("id") String stepId, @PathParam("destinationTitle") String destinationTitle,
            @PathParam("errortext") String errorMessage) {
        Date myDate = new Date();
        ReportProblemResponse response = new ReportProblemResponse();

        int sourceid = Integer.parseInt(stepId);

        response.setErrorStepId(sourceid);

        try {
            Step source = StepManager.getStepById(sourceid);
            if (source == null ) {
                response.setErrorText("StepId not found");
                return Response.status(Status.BAD_REQUEST).entity(response).build();
            }
            response.setErrorStepName(source.getTitel());

            source.setBearbeitungsstatusEnum(StepStatus.LOCKED);
            source.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
            source.setBearbeitungszeitpunkt(new Date());
            // Benutzer ben = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            // if (ben != null) {
            // source.setBearbeitungsbenutzer(ben);
            // }
            source.setBearbeitungsbeginn(null);
            Step temp = null;
            for (Step s : source.getProzess().getSchritteList()) {
                if (s.getTitel().equals(destinationTitle)) {
                    temp = s;
                }
            }
            if (temp != null) {
                response.setDestinationStepId(temp.getId());
                response.setDestinationStepName(temp.getTitel());

                temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
                temp.setCorrectionStep();
                temp.setBearbeitungsende(null);

                LogEntry logEntry = new LogEntry();
                logEntry.setContent(Helper.getTranslation("Korrektur notwendig") + " [automatic] " + errorMessage);
                logEntry.setCreationDate(myDate);
                logEntry.setProcessId(temp.getProzess().getId());
                logEntry.setType(LogType.ERROR);

                logEntry.setUserName("webapi");

                ProcessManager.saveLogEntry(logEntry);

                HistoryManager.addHistory(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError.getValue(), temp
                        .getProzess().getId());

                List<Step> alleSchritteDazwischen = new ArrayList<>();
                for (Step s : source.getProzess().getSchritteList()) {
                    if (s.getReihenfolge() <= source.getReihenfolge() && s.getReihenfolge() > temp.getReihenfolge()) {
                        alleSchritteDazwischen.add(s);
                    }
                }


                for (Iterator<Step> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
                    Step step = iter.next();
                    step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
                    // if (step.getPrioritaet().intValue() == 0)
                    step.setCorrectionStep();
                    step.setBearbeitungsende(null);
                    ErrorProperty seg = new ErrorProperty();
                    seg.setTitel(Helper.getTranslation("Korrektur notwendig"));
                    seg.setWert(Helper.getTranslation("KorrekturFuer") + temp.getTitel() + ": " + errorMessage);
                    seg.setSchritt(step);
                    seg.setType(PropertyType.messageImportant);
                    seg.setCreationDate(new Date());
                    step.getEigenschaften().add(seg);

                    StepManager.saveStep(step);
                }
                ProcessManager.saveProcess(source.getProzess());
                response.setProcessId(source.getProzess().getId());
                response.setProcessName(source.getProzess().getTitel());
                // pdao.save(source.getProzess());
            } else {
                response.setErrorText("Destination step not found");
                return Response.status(Status.BAD_REQUEST).entity(response).build();
            }

        } catch (DAOException e) {

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

        return Response.ok().entity(response).build();
    }


    @POST
    @Path("/reportproblem")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getReportProblemForTask( ReportProblem problem) {
        return getReportProblemForTask(problem.getStepId(), problem.getDestinationStepName(), problem.getErrorText());
    }

}
