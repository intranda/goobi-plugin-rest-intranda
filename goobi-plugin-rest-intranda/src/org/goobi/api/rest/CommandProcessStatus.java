package org.goobi.api.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.goobi.api.rest.response.ProcessStatusResponse;
import org.goobi.api.rest.response.StepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.persistence.managers.ProcessManager;

@Path("/process-status")
public class CommandProcessStatus {

	@Context
	UriInfo uriInfo;

	@Path("/json/{processid}")
	@GET
	@Produces("text/json")
	public ProcessStatusResponse getProcessStatusAsJson(@PathParam("processid") int processid) {
		ProcessStatusResponse resp = getData(processid);
		return resp;
	}

	@Path("/xml/{processid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public ProcessStatusResponse getProcessStatusAsXml(@PathParam("processid") int processid) {
		ProcessStatusResponse resp = getData(processid);
		return resp;
	}

	@Path("/html/{processid}")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public ProcessStatusResponse getProcessStatusAsHtml(@PathParam("processid") int processid) {
		ProcessStatusResponse resp = getData(processid);
		return resp;
	}

	private ProcessStatusResponse getData(int processid) {
		Process p = ProcessManager.getProcessById(processid);
		ProcessStatusResponse resp = new ProcessStatusResponse();
		if (p == null) {
			resp.setResult("No proccess with id " + processid + " found");
		} else {
			resp.setResult("ok");
			resp.setCreationDate(p.getErstellungsdatum());
			resp.setId(p.getId());
			resp.setTitle(p.getTitel());

			for (Step step : p.getSchritte()) {
				StepResponse sr = new StepResponse();
				resp.getStep().add(sr);
				sr.setEndDate(step.getBearbeitungsende());
				sr.setStartDate(step.getBearbeitungsbeginn());
				sr.setStatus(step.getBearbeitungsstatusEnum().getTitle());
				if (step.getBearbeitungsbenutzer() != null) {
					sr.setUser(step.getBearbeitungsbenutzer().getNachVorname());
				}
				sr.setTitle(step.getTitel());
				sr.setOrder(step.getReihenfolge());
			}
		}
		return resp;
	}
}
