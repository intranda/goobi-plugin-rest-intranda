package org.goobi.api.rest;

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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.goobi.api.rest.request.CreationRequest;
import org.goobi.api.rest.request.StanfordCreationRequest;
import org.goobi.api.rest.response.CreationResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IOpacPlugin;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import lombok.extern.log4j.Log4j;

@Path("/process")
@Log4j
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

    // TODO wieder entfernen. get ist nicht erlaubt. Entweder POST oder PUT
    @Path("create/{templateid}/{catalogueid}")
    @GET
    @Produces("text/json")
    public CreationResponse createNewProcess(@PathParam("templateid") int templateId, @PathParam("catalogueid") String catalogueId) {
        return createNewProcess(templateId, "GBV", catalogueId);
    }

    // TODO wieder entfernen. get ist nicht erlaubt. Entweder POST oder PUT
    @Path("create/{templateid}/{catalogue}/{catalogueid}")
    @GET
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
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    public CreationResponse createProcessForStanford(StanfordCreationRequest req, @Context final HttpServletResponse response) {
        CreationResponse cr = new CreationResponse();

        Process p = ProcessManager.getProcessByTitle(req.getSourceID());
        if (p != null) {
            cr.setResult("error");
            cr.setErrorText("Process " + req.getSourceID() + " already exists.");
            cr.setProcessId(p.getId());
            cr.setProcessName(p.getTitel());
            return cr;
        }
        
        Process template = ProcessManager.getProcessByTitle(req.getWorkflowName());
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

            Metadata title = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
            title.setValue(req.getObjectLabel());
            logical.addMetadata(title);
            
            Metadata identifierDigital = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
            identifierDigital.setValue(req.getObjectID());
            logical.addMetadata(identifierDigital);
            
            Metadata identifierSource = new Metadata(prefs.getMetadataTypeByName("CatalogIDSource"));
            identifierSource.setValue(req.getSourceID());
            logical.addMetadata(identifierSource);
            
            Metadata classification = new Metadata(prefs.getMetadataTypeByName("Classification"));
            classification.setValue(req.getTag_Process());
            logical.addMetadata(classification);
            
        } catch (UGHException e) {
            cr.setResult("error");
            cr.setErrorText("Error during metadata creation for " + req.getSourceID() + ": " + e.getMessage());
            return cr;
        }
        Process process = cloneTemplate(template);
        // set title
        process.setTitel(req.getSourceID());

        try {
            NeuenProzessAnlegen(process, template, fileformat, prefs);
        } catch (Exception e) {
            cr.setResult("error");
            cr.setErrorText("Error during process creation for " + req.getSourceID() + ": " + e.getMessage());
            return cr;
        }
        
        Processproperty idSource = new Processproperty();
        idSource.setTitel("source_id");
        idSource.setWert(req.getSourceID());
        idSource.setProcessId(process.getId());
        PropertyManager.saveProcessProperty(idSource);

        Processproperty idObject = new Processproperty();
        idObject.setTitel("object_id");
        idObject.setWert(req.getObjectID());
        idObject.setProcessId(process.getId());
        PropertyManager.saveProcessProperty(idObject);
        
        Processproperty labelObject = new Processproperty();
        labelObject.setTitel("object_label");
        labelObject.setWert(req.getObjectLabel());
        labelObject.setProcessId(process.getId());
        PropertyManager.saveProcessProperty(labelObject);
        
        Processproperty tagProcess = new Processproperty();
        tagProcess.setTitel("tag_process");
        tagProcess.setWert(req.getTag_Process());
        tagProcess.setProcessId(process.getId());
        PropertyManager.saveProcessProperty(tagProcess);
        
        Processproperty tagProject = new Processproperty();
        tagProject.setTitel("tag_project");
        tagProject.setWert(req.getTag_Project());
        tagProject.setProcessId(process.getId());
        PropertyManager.saveProcessProperty(tagProject);
        
        Processproperty tagContentType = new Processproperty();
        tagContentType.setTitel("tag_contentType");
        tagContentType.setWert(req.getTag_ContentType());
        tagContentType.setProcessId(process.getId());
        PropertyManager.saveProcessProperty(tagContentType);
        
        cr.setResult("success");
        cr.setProcessName(process.getTitel());
        cr.setProcessId(process.getId());
        return cr;
    }

    //    @Path("/test")
    //    @POST
    //    @Consumes(MediaType.TEXT_XML)
    //    public CreationResponse createNewProcessPost(CreationRequest req, @Context final HttpServletResponse response) {
    //        CreationResponse cr = new CreationResponse();
    //        String errorText = req.validateRequest(req);
    //
    //        if (!errorText.isEmpty()) {
    //            cr.setResult("failure");
    //            cr.setErrorText(errorText);
    //            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    //            return cr;
    //        }
    //
    //        String opacIdentifier = req.getIdentifier();
    //
    //        String processTitle = req.getOrder_number() + "_" + req.getItem_in_order() + req.getLastname();
    //
    //        Process p = ProcessManager.getProcessByTitle(processTitle);
    //        if (p != null) {
    //            cr.setResult("failure");
    //            cr.setErrorText("Process " + processTitle + " already exists.");
    //            cr.setProcessId(p.getId());
    //            cr.setProcessName(p.getTitel());
    //            response.setStatus(HttpServletResponse.SC_CONFLICT);
    //            return cr;
    //        }
    //
    //        Process template = ProcessManager.getProcessById(req.getProcess_template());
    //        Prefs prefs = template.getRegelsatz().getPreferences();
    //        Fileformat ff = null;
    //        try {
    //            ff = getOpacRequest(opacIdentifier, prefs);
    //
    //        } catch (Exception e) {
    //            cr.setResult("failure");
    //            cr.setErrorText("Error during opac request for " + opacIdentifier + ": " + e.getMessage());
    //            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    //            return cr;
    //        }
    //
    //        Process process = cloneTemplate(template);
    //        // set title
    //        process.setTitel(processTitle);
    //
    //        try {
    //            NeuenProzessAnlegen(process, template, ff, prefs);
    //        } catch (Exception e) {
    //            cr.setResult("failure");
    //            cr.setErrorText("Error during process creation for " + opacIdentifier + ": " + e.getMessage());
    //            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    //            return cr;
    //        }
    //
    //        createProperties(req, process);
    //
    //        cr.setResult("success");
    //        cr.setProcessName(process.getTitel());
    //        cr.setProcessId(process.getId());
    //        return cr;
    //
    //    }

    private void createProperties(CreationRequest req, Process process) {
        int id = process.getId();

        if (StringUtils.isNotBlank(req.getSignature())) {
            Processproperty pp = new Processproperty();
            pp.setTitel("shelfmark");
            pp.setWert(req.getSignature());
            pp.setProcessId(id);
            PropertyManager.saveProcessProperty(pp);
        }

        Processproperty identifier = new Processproperty();
        identifier.setTitel("identifier");
        identifier.setWert(req.getIdentifier());
        identifier.setProcessId(id);
        PropertyManager.saveProcessProperty(identifier);

        Processproperty lastname = new Processproperty();
        lastname.setTitel("lastname");
        lastname.setWert(req.getLastname());
        lastname.setProcessId(id);
        PropertyManager.saveProcessProperty(lastname);

        Processproperty salutation = new Processproperty();
        salutation.setTitel("salutation");
        salutation.setWert(req.getSalutation());
        salutation.setProcessId(id);
        PropertyManager.saveProcessProperty(salutation);

        Processproperty email = new Processproperty();
        email.setTitel("email");
        email.setWert(req.getEmail());
        email.setProcessId(id);
        PropertyManager.saveProcessProperty(email);

        Processproperty order_number = new Processproperty();
        order_number.setTitel("order_number");
        order_number.setWert(String.valueOf(req.getOrder_number()));
        order_number.setProcessId(id);
        PropertyManager.saveProcessProperty(order_number);

        Processproperty item_in_order = new Processproperty();
        item_in_order.setTitel("item_in_order");
        item_in_order.setWert(String.valueOf(req.getItem_in_order()));
        item_in_order.setProcessId(id);
        PropertyManager.saveProcessProperty(item_in_order);

        if (req.getBtw_number() != 0) {
            Processproperty btw_number = new Processproperty();
            btw_number.setTitel("lastname");
            btw_number.setWert(String.valueOf(req.getBtw_number()));
            btw_number.setProcessId(id);
            PropertyManager.saveProcessProperty(btw_number);
        }

        Processproperty all_pages = new Processproperty();
        all_pages.setTitel("all_pages");
        all_pages.setWert(String.valueOf(req.isAll_pages()));
        all_pages.setProcessId(id);
        PropertyManager.saveProcessProperty(all_pages);

        Processproperty page_numbers = new Processproperty();
        page_numbers.setTitel("page_numbers");
        page_numbers.setWert(req.getPage_numbers());
        page_numbers.setProcessId(id);
        PropertyManager.saveProcessProperty(page_numbers);

        if (StringUtils.isNotBlank(req.getClient_instructions())) {
            Processproperty client_instructions = new Processproperty();
            client_instructions.setTitel("client_instructions");
            client_instructions.setWert(req.getClient_instructions());
            client_instructions.setProcessId(id);
            PropertyManager.saveProcessProperty(client_instructions);
        }

        List<StringPair> metadata = MetadataManager.getMetadata(id);

        for (StringPair sp : metadata) {
            if (sp.getOne().equals("TitleDocMain")) {
                Processproperty title = new Processproperty();
                title.setTitel("displayed title");
                title.setWert(sp.getTwo());
                title.setProcessId(id);
                PropertyManager.saveProcessProperty(title);
            } else if (sp.getOne().equals("ListOfCreators")) {
                Processproperty title = new Processproperty();
                title.setTitel("displayed authors");
                title.setWert(sp.getTwo());
                title.setProcessId(id);
                PropertyManager.saveProcessProperty(title);
            } else if (sp.getOne().equals("PublicationYear")) {
                Processproperty title = new Processproperty();
                title.setTitel(" publication year");
                title.setWert(sp.getTwo());
                title.setProcessId(id);
                PropertyManager.saveProcessProperty(title);
            } else if (sp.getOne().equals("shelfmarksource")) {
                Processproperty title = new Processproperty();
                title.setTitel(" displayed shelfmark");
                title.setWert(sp.getTwo());
                title.setProcessId(id);
                PropertyManager.saveProcessProperty(title);
            }

        }
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
        // TODO
        // get logical data from opac
        ConfigOpacCatalogue coc = new ConfigOpac().getCatalogueByName(myCatalogue);
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

}
