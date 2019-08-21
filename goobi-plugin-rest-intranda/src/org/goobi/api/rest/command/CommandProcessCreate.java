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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.goobi.api.rest.request.CreationRequest;
import org.goobi.api.rest.request.MpiCreationRequest;
import org.goobi.api.rest.request.StanfordCreationRequest;
import org.goobi.api.rest.request.StanfordCreationRequestTag;
import org.goobi.api.rest.response.CreationResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IOpacPlugin;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

@Path("/process")

public class CommandProcessCreate {

    @Context
    UriInfo uriInfo;

    @Path("/testresponse")
    @GET
    @Produces(MediaType.TEXT_XML)
    public CreationResponse helloWorld() {
        CreationResponse cr = new CreationResponse();
        cr.setProcessId(123);
        cr.setErrorText("field order_number is missing or empty");
        cr.setProcessName("34_Doe");
        cr.setResult("failure");
        return cr;
    }

    @Path("/testrequest")
    @GET
    @Produces(MediaType.TEXT_XML)
    public CreationRequest getTest(@PathParam("text") String text) {
        CreationRequest cr = new CreationRequest();
        cr.setAll_pages(true);
        cr.setBtw_number(999999999);
        cr.setClient_instructions("some instructions");
        cr.setEmail("john.doe@example.com");
        cr.setIdentifier("003192975");
        cr.setItem_in_order(12);
        cr.setLastname("Doe");
        cr.setOrder_number(34);
        cr.setPage_numbers("1-7");
        cr.setProcess_template(1);
        cr.setSalutation("Mr.");
        cr.setSignature("otm: kf 62-335");

        return cr;
    }

    @Path("create/{templateid}/{catalogueid}")
    @POST
    @Produces("text/json")
    public CreationResponse createNewProcess(@PathParam("templateid") int templateId, @PathParam("catalogueid") String catalogueId) {
        return createNewProcess(templateId, "GBV", catalogueId);
    }

    @Path("create/{templateid}/{catalogue}/{catalogueid}")
    @POST
    @Produces("text/json")
    public CreationResponse createNewProcess(@PathParam("templateid") int templateId, @PathParam("catalogue") String catalogue,
            @PathParam("catalogueid") String catalogueId) {
        CreationResponse cr = new CreationResponse();

        String opacIdentifier = catalogueId;
        String myCatalogue = catalogue;
        String processTitle = catalogueId;

        Process p = ProcessManager.getProcessByTitle(processTitle);
        if (p != null) {
            cr.setResult("error");
            cr.setErrorText("Process " + processTitle + " already exists.");
            cr.setProcessId(p.getId());
            cr.setProcessName(p.getTitel());
            return cr;
        }

        Process template = ProcessManager.getProcessById(templateId);
        Prefs prefs = template.getRegelsatz().getPreferences();
        Fileformat ff = null;
        try {
            ff = getOpacRequest(opacIdentifier, prefs, myCatalogue);

        } catch (Exception e) {
            cr.setResult("error");
            cr.setErrorText("Error during opac request for " + opacIdentifier + " from catalogue " + myCatalogue + ": " + e.getMessage());
            return cr;
        }

        Process process = cloneTemplate(template);
        // set title
        process.setTitel(processTitle);

        try {
            NeuenProzessAnlegen(process, template, ff, prefs);
        } catch (Exception e) {
            cr.setResult("error");
            cr.setErrorText("Error during process creation for " + opacIdentifier + ": " + e.getMessage());
            return cr;
        }

        cr.setResult("success");
        cr.setProcessName(process.getTitel());
        cr.setProcessId(process.getId());
        return cr;
    }

    @Path("/stanfordcreate")
    @POST
    @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    @Produces(MediaType.TEXT_XML)
    public Response createProcessForStanford(StanfordCreationRequest req, @Context final HttpServletResponse response) {
        CreationResponse cr = new CreationResponse();
        String processtitle = UghHelper.convertUmlaut(req.getObjectId().replace("druid:", "")).toLowerCase();
        processtitle += "_" + UghHelper.convertUmlaut(req.getSourceID().replace(":", "_")).toLowerCase();
        processtitle.replaceAll("[\\W]", "");

        Process p = ProcessManager.getProcessByTitle(processtitle);
        if (p != null) {
            cr.setResult("error");
            cr.setErrorText("Process " + req.getSourceID() + " already exists.");
            cr.setProcessId(p.getId());
            cr.setProcessName(p.getTitel());
            //            response.setStatus(HttpServletResponse.SC_CONFLICT);
            Response resp = Response.status(Response.Status.CONFLICT).entity(cr).build();
            return resp;
        }

        Process template = ProcessManager.getProcessByTitle(req.getGoobiWorkflow());
        if (template == null) {
            cr.setResult("error");
            cr.setErrorText("Process template " + req.getGoobiWorkflow() + " does not exist.");
            cr.setProcessId(0);
            cr.setProcessName(req.getSourceID());
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(cr).build();
            return resp;
        }

        Prefs prefs = template.getRegelsatz().getPreferences();
        Fileformat fileformat = null;
        try {
            fileformat = new MetsMods(prefs);
            DigitalDocument digDoc = new DigitalDocument();
            fileformat.setDigitalDocument(digDoc);
            DocStruct logical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
            DocStruct physical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
            digDoc.setLogicalDocStruct(logical);
            digDoc.setPhysicalDocStruct(physical);

            // metadata
            if (StringUtils.isNotBlank(req.getTitle())) {
                Metadata title = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
                title.setValue(req.getTitle());
                logical.addMetadata(title);
            }
            Metadata identifierDigital = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
            identifierDigital.setValue(req.getObjectId());
            logical.addMetadata(identifierDigital);
            if (StringUtils.isNotBlank(req.getSourceID())) {
                Metadata identifierSource = new Metadata(prefs.getMetadataTypeByName("CatalogIDSource"));
                identifierSource.setValue(req.getSourceID());
                logical.addMetadata(identifierSource);
            }
            if (StringUtils.isNotBlank(req.getCollectionName())) {
                Metadata classification = new Metadata(prefs.getMetadataTypeByName("singleDigCollection"));
                classification.setValue(req.getCollectionName());
                logical.addMetadata(classification);
            }
        } catch (UGHException e) {
            cr.setResult("error");
            cr.setErrorText("Error during metadata creation for " + req.getSourceID() + ": " + e.getMessage());
            Response resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cr).build();
            return resp;
        }
        Process process = cloneTemplate(template);
        // set title
        process.setTitel(processtitle);

        if (StringUtils.isNotBlank(req.getProject())) {
            List<Project> projects = ProjectManager.getAllProjects();
            for (Project proj : projects) {
                if (proj.getTitel().equals(req.getProject())) {
                    process.setProjekt(proj);
                }
            }
        }

        try {
            NeuenProzessAnlegen(process, template, fileformat, prefs);
        } catch (Exception e) {
            cr.setResult("error");
            cr.setErrorText("Error during process creation for " + req.getSourceID() + ": " + e.getMessage());
            Response resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cr).build();
            return resp;
        }
        if (StringUtils.isNotBlank(req.getObjectId())) {
            setProperty(process.getId(), "objectId", req.getObjectId(), true);
            setProperty(process.getId(), "Argo URL", "https://argo.stanford.edu/view/" + req.getObjectId(), true);
            setProperty(process.getId(), "PURL", "https://purl.stanford.edu/" + req.getObjectId().replace("druid:", ""), true);

        }
        setProperty(process.getId(), "objectType", req.getObjectType(), true);
        setProperty(process.getId(), "sourceID", req.getSourceID(), true);
        setProperty(process.getId(), "title", req.getTitle(), true);
        setProperty(process.getId(), "contentType", req.getContentType(), true);
        setProperty(process.getId(), "project", req.getProject(), true);
        setProperty(process.getId(), "catkey", req.getCatkey(), true);
        setProperty(process.getId(), "barcode", req.getBarcode(), true);
        setProperty(process.getId(), "collectionId", req.getCollectionId(), true);
        setProperty(process.getId(), "collectionName", req.getCollectionName(), true);
        setProperty(process.getId(), "sdrWorkflow", req.getSdrWorkflow(), true);
        setProperty(process.getId(), "goobiWorkflow", req.getGoobiWorkflow(), true);
        // if OCR shall be done
        String ocr = "false";
        if (StringUtils.isNotBlank(req.getOcr())) {
            ocr = req.getOcr();
        }
        setProperty(process.getId(), "OCR", ocr, true);

        if (req.getTags() != null && req.getTags().size() > 0) {
            for (StanfordCreationRequestTag tag : req.getTags()) {
                if (StringUtils.isNotBlank(tag.getName())) {
                    setProperty(process.getId(), tag.getName(), tag.getValue(), false);
                }
            }
        }

        // add template name information
        setProperty(process.getId(), "Template", template.getTitel(), true);
        // add template ID information
        setProperty(process.getId(), "TemplateID", template.getId().toString(), true);

        cr.setResult("success");
        cr.setProcessName(process.getTitel());
        cr.setProcessId(process.getId());
        Response resp = Response.status(Response.Status.CREATED).entity(cr).build();
        return resp;
    }

    @Path("/mpicreate")
    @POST
    @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    @Produces(MediaType.TEXT_XML)
    public Response createProcessForMPI(MpiCreationRequest req, @Context final HttpServletResponse response) {
        CreationResponse cr = new CreationResponse();
        String processtitle = UghHelper.convertUmlaut(req.getBarcode().replace(":", "_")).toLowerCase();
        processtitle.replaceAll("[\\W]", "");

        Process p = ProcessManager.getProcessByTitle(processtitle);
        if (p != null) {
            cr.setResult("error");
            cr.setErrorText("Process " + req.getBarcode() + " already exists.");
            cr.setProcessId(p.getId());
            cr.setProcessName(p.getTitel());
            //            response.setStatus(HttpServletResponse.SC_CONFLICT);
            Response resp = Response.status(Response.Status.CONFLICT).entity(cr).build();
            return resp;
        }

        Process template = ProcessManager.getProcessByTitle(req.getGoobiWorkflow());
        if (template == null) {
            cr.setResult("error");
            cr.setErrorText("Process template " + req.getGoobiWorkflow() + " does not exist.");
            cr.setProcessId(0);
            cr.setProcessName(req.getBarcode());
            Response resp = Response.status(Response.Status.BAD_REQUEST).entity(cr).build();
            return resp;
        }

        Prefs prefs = template.getRegelsatz().getPreferences();
        Fileformat fileformat = null;
        try {
            fileformat = new MetsMods(prefs);
            DigitalDocument digDoc = new DigitalDocument();
            fileformat.setDigitalDocument(digDoc);
            DocStruct logical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("Monograph"));
            DocStruct physical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
            digDoc.setLogicalDocStruct(logical);
            digDoc.setPhysicalDocStruct(physical);

            // metadata
            if (StringUtils.isNotBlank(req.getTitel())) {
                Metadata title = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
                title.setValue(req.getTitel());
                logical.addMetadata(title);
            }
            Metadata identifierDigital = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
            identifierDigital.setValue(req.getBarcode());
            logical.addMetadata(identifierDigital);

        } catch (UGHException e) {
            cr.setResult("error");
            cr.setErrorText("Error during metadata creation for " + req.getBarcode() + ": " + e.getMessage());
            Response resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cr).build();
            return resp;
        }
        Process process = cloneTemplate(template);
        // set title
        process.setTitel(processtitle);

        try {
            NeuenProzessAnlegen(process, template, fileformat, prefs);
        } catch (Exception e) {
            cr.setResult("error");
            cr.setErrorText("Error during process creation for " + req.getBarcode() + ": " + e.getMessage());
            Response resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(cr).build();
            return resp;
        }
        if (StringUtils.isNotBlank(req.getBarcode())) {
            Processproperty idObject = new Processproperty();
            idObject.setTitel("barcode");
            idObject.setWert(req.getBarcode());
            idObject.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(idObject);
        }
        if (StringUtils.isNotBlank(req.getUser())) {
            Processproperty objectType = new Processproperty();
            objectType.setTitel("user");
            objectType.setWert(req.getUser());
            objectType.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(objectType);
        }
        if (StringUtils.isNotBlank(req.getSignatur())) {
            Processproperty idSource = new Processproperty();
            idSource.setTitel("signatur");
            idSource.setWert(req.getSignatur());
            idSource.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(idSource);
        }
        if (StringUtils.isNotBlank(req.getTitel())) {
            Processproperty labelObject = new Processproperty();
            labelObject.setTitel("titel");
            labelObject.setWert(req.getTitel());
            labelObject.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(labelObject);
        }
        if (StringUtils.isNotBlank(req.getBestand())) {
            Processproperty tagProcess = new Processproperty();
            tagProcess.setTitel("bestand");
            tagProcess.setWert(req.getBestand());
            tagProcess.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(tagProcess);
        }
        if (StringUtils.isNotBlank(req.getArchiv())) {
            Processproperty tagProject = new Processproperty();
            tagProject.setTitel("archiv");
            tagProject.setWert(req.getArchiv());
            tagProject.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(tagProject);
        }
        if (StringUtils.isNotBlank(req.getKommentar())) {
            Processproperty catkey = new Processproperty();
            catkey.setTitel("kommentar");
            catkey.setWert(req.getKommentar());
            catkey.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(catkey);
        }
        if (StringUtils.isNotBlank(req.getGoobiWorkflow())) {
            Processproperty goobiWorkflow = new Processproperty();
            goobiWorkflow.setTitel("goobiWorkflow");
            goobiWorkflow.setWert(req.getGoobiWorkflow());
            goobiWorkflow.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(goobiWorkflow);
        }

        if (StringUtils.isNotBlank(req.getDjangoWorkflow())) {
            Processproperty djangoWorkflow = new Processproperty();
            djangoWorkflow.setTitel("djangoWorkflow");
            djangoWorkflow.setWert(req.getDjangoWorkflow());
            djangoWorkflow.setProcessId(process.getId());
            PropertyManager.saveProcessProperty(djangoWorkflow);
        }

        cr.setResult("success");
        cr.setProcessName(process.getTitel());
        cr.setProcessId(process.getId());
        Response resp = Response.status(Response.Status.CREATED).entity(cr).build();
        return resp;
    }

    private Process cloneTemplate(Process template) {
        Process process = new Process();

        process.setIstTemplate(false);
        process.setInAuswahllisteAnzeigen(false);
        process.setProjekt(template.getProjekt());
        process.setRegelsatz(template.getRegelsatz());
        process.setDocket(template.getDocket());

        BeanHelper bHelper = new BeanHelper();
        bHelper.SchritteKopieren(template, process);
        bHelper.ScanvorlagenKopieren(template, process);
        bHelper.WerkstueckeKopieren(template, process);
        bHelper.EigenschaftenKopieren(template, process);

        return process;
    }

    private Fileformat getOpacRequest(String opacIdentifier, Prefs prefs, String myCatalogue) throws Exception {
        // get logical data from opac
        ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(myCatalogue);
        //        ConfigOpacCatalogue coc = new ConfigOpac().getCatalogueByName(myCatalogue);
        IOpacPlugin myImportOpac = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, coc.getOpacType());
        Fileformat ff = myImportOpac.search("12", opacIdentifier, coc, prefs);
        Metadata md = new Metadata(prefs.getMetadataTypeByName("singleDigCollection"));
        md.setValue("DefaultCollection");
        DocStruct log = ff.getDigitalDocument().getLogicalDocStruct();
        log.addMetadata(md);
        MetadataType sourceType = prefs.getMetadataTypeByName("CatalogIDSource");
        MetadataType digType = prefs.getMetadataTypeByName("CatalogIDDigital");
        if (log.getAllMetadataByType(sourceType).isEmpty()) {
            Metadata source = new Metadata(sourceType);
            source.setValue(opacIdentifier);
            log.addMetadata(source);
        }

        if (log.getAllMetadataByType(digType).isEmpty()) {
            Metadata source = new Metadata(digType);
            source.setValue(opacIdentifier);
            log.addMetadata(source);
        }

        return ff;
    }

    public String NeuenProzessAnlegen(Process process, Process template, Fileformat ff, Prefs prefs) throws Exception {

        for (Step step : process.getSchritteList()) {

            step.setBearbeitungszeitpunkt(process.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            LoginBean loginForm = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
            if (loginForm != null) {
                step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
            }

            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(process.getErstellungsdatum());

                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }

        }

        ProcessManager.saveProcess(process);

        /*
         * -------------------------------- Imagepfad hinzufügen (evtl. vorhandene zunächst löschen) --------------------------------
         */
        try {
            MetadataType mdt = prefs.getMetadataTypeByName("pathimagefiles");
            List<? extends Metadata> alleImagepfade = ff.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
            if (alleImagepfade != null && alleImagepfade.size() > 0) {
                for (Metadata md : alleImagepfade) {
                    ff.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                }
            }
            Metadata newmd = new Metadata(mdt);
            if (SystemUtils.IS_OS_WINDOWS) {
                newmd.setValue("file:/" + process.getImagesDirectory() + process.getTitel().trim() + "_tif");
            } else {
                newmd.setValue("file://" + process.getImagesDirectory() + process.getTitel().trim() + "_tif");
            }
            ff.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);

            /* Rdf-File schreiben */
            process.writeMetadataFile(ff);

        } catch (ugh.exceptions.DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
            return e.getMessage();
        }

        // Adding process to history
        HistoryAnalyserJob.updateHistoryForProzess(process);

        ProcessManager.saveProcess(process);

        process.readMetadataFile();

        List<Step> steps = StepManager.getStepsForProcess(process.getId());
        for (Step s : steps) {
            if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN) && s.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                myThread.start();
            }
        }
        return "";
    }

    private void setProperty(int processId, String name, String value, boolean skipIfExistsAlready) {
        // empty values shall not be saved
        if (StringUtils.isBlank(value)) {
            return;
        }

        // if existing values shall not be created again
        if (skipIfExistsAlready) {
            List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processId);
            for (Processproperty p : pps) {
                if (p.getTitel().equals(name)) {
                    return;
                }
            }
        }

        // create (new) property
        Processproperty pp = new Processproperty();
        Process p = ProcessManager.getProcessById(processId);
        pp.setProcessId(processId);
        pp.setTitel(name);
        pp.setWert(value);
        PropertyManager.saveProcessProperty(pp);
    }

}
