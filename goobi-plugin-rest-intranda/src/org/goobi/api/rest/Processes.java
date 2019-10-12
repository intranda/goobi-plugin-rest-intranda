package org.goobi.api.rest;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.AddProcessMetadataReq;
import org.goobi.api.rest.request.DeleteProcessMetadataReq;
import org.goobi.api.rest.request.ProcessCreationRequest;
import org.goobi.api.rest.request.SearchGroup;
import org.goobi.api.rest.request.SearchQuery;
import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.api.rest.response.CreationResponse;
import org.goobi.api.rest.response.ProcessStatusResponse;
import org.goobi.api.rest.response.StepResponse;
import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IOpacPlugin;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ImportPluginException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import lombok.extern.log4j.Log4j;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;

@Log4j
@Path("/processes")
@Produces(MediaType.APPLICATION_JSON)
public class Processes {
    @Context
    HttpServletRequest request;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{processId}/images/{folder}")
    public Response uploadFile(@PathParam("processId") int processId, @PathParam("folder") final String folder,
            @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData) {
        Process p = ProcessManager.getProcessById(processId);
        HttpSession session = request.getSession();
        LoginBean userBean = (LoginBean) session.getAttribute("LoginForm");
        User user = null;
        if (userBean != null) {
            user = userBean.getMyBenutzer();
        }
        if (user != null) {
            // authorized as user - check wether user is assigned to project
            int stepProjectId = p.getProjectId();
            boolean userInProject = user.getProjekte().stream().map(proj -> proj.getId()).anyMatch(projectId -> projectId == stepProjectId);
            if (!userInProject) {
                return Response.status(401).build();
            }
            // TODO: maybe more checks
        }
        String destFolder = null;
        try {
            if ("master".equals(folder)) {
                destFolder = p.getImagesOrigDirectory(false);
            } else {
                destFolder = p.getImagesTifDirectory(false);
            }
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        java.nio.file.Path path = Paths.get(destFolder);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            try {
                StorageProvider.getInstance().createDirectories(path);
            } catch (IOException e) {
                log.error(e);
                return Response.status(500).build();
            }
        }

        try {
            StorageProvider.getInstance().uploadFile(fileInputStream, path.resolve(fileMetaData.getFileName()));
        } catch (IOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProcess(ProcessCreationRequest req) {

        /**
         * START check if all parameters are fine
         */
        if (StringUtils.isBlank(req.getIdentifier())) {
            // abort and send error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("identifier may not be blank");
            return Response.status(400).entity(resp).build();
        }
        if (StringUtils.isBlank(req.getTemplateName()) && req.getTemplateId() == null) {
            // abort and send error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("templateName or templateId are mandatory");
            return Response.status(400).entity(resp).build();
        }
        boolean useOpac = req.getOpacConfig() != null;
        if (!useOpac && StringUtils.isBlank(req.getLogicalDSType())) {
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("when not getting metadata from the catalogue, \"logicaDSType\" is mandatory");
            return Response.status(400).entity(resp).build();
        }
        String processTitle = StringUtils.isBlank(req.getProcesstitle()) ? req.getIdentifier() : req.getProcesstitle();
        Process p = ProcessManager.getProcessByTitle(processTitle);
        if (p != null) {
            // abort and send error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText(String.format("Process with title \"%s\" is already present in Goobi", processTitle));
            return Response.status(409).entity(resp).build();
        }
        /**
         * END check if all parameters are fine
         */

        /**
         * START creating the process
         */
        Process template = null;
        if (req.getTemplateId() != null) {
            template = ProcessManager.getProcessById(req.getTemplateId());
        } else if (req.getTemplateName() != null) {
            template = ProcessManager.getProcessByExactTitle(req.getTemplateName());
        }
        if (template == null) {
            // return error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Could not find template with provided templateId or templateName");
            return Response.status(404).entity(resp).build();
        }

        p = cloneTemplate(template);
        p.setTitel(processTitle);

        /**
         * handle metadata stuff
         */
        Prefs prefs = null;
        Fileformat fileformat = null;
        try {
            prefs = template.getRegelsatz().getPreferences();
            if (useOpac) {
                fileformat =
                        getRecordFromCatalogue(prefs, req.getIdentifier(), req.getOpacConfig().getOpacName(), req.getOpacConfig().getSearchField());
            } else {
                fileformat = new MetsMods(prefs);
            }
        } catch (PreferencesException e) {
            // send error message 
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Could not read metadata ruleset");
            return Response.status(500).entity(resp).build();
        } catch (ImportPluginException e) {
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText(e.getMessage());
            return Response.status(500).entity(resp).build();
        }
        DigitalDocument digDoc;
        if (useOpac) {
            try {
                digDoc = fileformat.getDigitalDocument();
            } catch (PreferencesException e) {
                // TODO Auto-generated catch block
                log.error(e);
                return null;
            }
        } else {
            digDoc = new DigitalDocument();
            fileformat.setDigitalDocument(digDoc);
        }
        DocStruct logicalDs = null;
        try {
            if (useOpac) {
                logicalDs = digDoc.getLogicalDocStruct();
            } else {
                logicalDs = digDoc.createDocStruct(prefs.getDocStrctTypeByName(req.getLogicalDSType()));
                Metadata idMd = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
                idMd.setValue(req.getIdentifier());
                digDoc.setLogicalDocStruct(logicalDs);
            }
            if (digDoc.getPhysicalDocStruct() == null) {
                DocStruct physical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
                digDoc.setPhysicalDocStruct(physical);
            }
        } catch (TypeNotAllowedForParentException | MetadataTypeNotAllowedException e) {
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Error creating logical or physical DocStruct.");
            return Response.status(500).entity(resp).build();
        }
        if (req.getMetadata() != null) {
            Map<String, String> metadata = req.getMetadata();
            for (String key : metadata.keySet()) {
                // add metadata to new process
                MetadataType mdt = prefs.getMetadataTypeByName(key);
                if (mdt == null) {
                    // another good errormessage and return;
                    CreationResponse resp = new CreationResponse();
                    resp.setResult("error");
                    resp.setErrorText(String.format("Could not find MetadataType for \"%s\" in ruleset.", key));
                    return Response.status(422).entity(resp).build();
                }
                Metadata md;
                try {
                    md = new Metadata(mdt);
                    md.setValue(metadata.get(key));
                    logicalDs.addMetadata(md);
                } catch (MetadataTypeNotAllowedException e) {
                    // send good error message and return
                    log.error(e);
                    CreationResponse resp = new CreationResponse();
                    resp.setResult("error");
                    resp.setErrorText(String.format("MetadataType \"%s\" not allowed in logical DocStruct.", key));
                    return Response.status(422).entity(resp).build();
                }
            }
        }

        try {
            ProcessManager.saveProcess(p);
        } catch (DAOException e1) {
            // send 500 and error message
            log.error(e1);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Could not save process to database.");
            return Response.status(500).entity(resp).build();
        }
        //save template id and title 
        Processproperty processProp = new Processproperty();
        processProp.setProzess(p);
        processProp.setTitel("TemplateID");
        processProp.setWert(template.getId().toString());
        PropertyManager.saveProcessProperty(processProp);
        processProp = new Processproperty();
        processProp.setProzess(p);
        processProp.setTitel("Template");
        processProp.setWert(template.getTitel());
        PropertyManager.saveProcessProperty(processProp);
        if (req.getProperties() != null) {
            for (String key : req.getProperties().keySet()) {
                // add properties
                Processproperty pp = new Processproperty();
                pp.setProzess(p);
                pp.setTitel(key);
                pp.setWert(req.getProperties().get(key));
                PropertyManager.saveProcessProperty(pp);
            }
        }
        try {
            p.writeMetadataFile(fileformat);
        } catch (WriteException | PreferencesException | IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Error saving metadata file.");
            return Response.status(500).entity(resp).build();
        }
        /**
         * END creating the process
         */

        CreationResponse resp = new CreationResponse();
        resp.setResult("success");
        resp.setProcessId(p.getId());
        resp.setProcessName(p.getTitel());
        return Response.ok(resp).build();
    }

    @GET
    @Path("/search")
    public List<RestProcess> simpleSearch(@QueryParam("field") String field, @QueryParam("value") String value, @QueryParam("limit") int limit,
            @QueryParam("offset") int offset, @QueryParam("orderby") String sortField, @QueryParam("descending") boolean sortDescending,
            @QueryParam("filterProjects") String filterProjects) throws SQLException {
        SearchQuery query = new SearchQuery(field, value, RelationalOperator.LIKE);
        SearchGroup group = new SearchGroup();
        group.addFilter(query);
        SearchRequest req = new SearchRequest();
        req.addSearchGroup(group);
        req.setLimit(limit);
        req.setOffset(offset);
        req.setSortField(sortField);
        req.setSortDescending(sortDescending);
        if (filterProjects != null) {
            req.setFilterProjects(Arrays.asList(filterProjects.split(",")));
        }

        return req.search();
    }

    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<RestProcess> advancedSearch(SearchRequest sr) throws SQLException {
        return sr.search();
    }

    @DELETE
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse deleteMetadata(@PathParam("id") int processId, DeleteProcessMetadataReq req)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Process p = ProcessManager.getProcessById(processId);
        return req.apply(p);
    }

    @POST
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse addMetadata(@PathParam("id") int processId, AddProcessMetadataReq req)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Process p = ProcessManager.getProcessById(processId);
        return req.apply(p);
    }

    @PUT
    @Path("/{id}/properties/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProcessProperty(@PathParam("id") int processId, @PathParam("name") String name, String newValue) {
        List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processId);
        Processproperty pp = null;
        for (Processproperty p : pps) {
            if (p.getTitel().equals(name)) {
                pp = p;
                break;
            }
        }
        if (pp == null) {
            pp = new Processproperty();
            Process p = ProcessManager.getProcessById(processId);
            pp.setProzess(p);
            pp.setTitel(name);
        }
        pp.setWert(newValue);
        PropertyManager.saveProcessProperty(pp);
        return Response.accepted().build();
    }

    @Path("/ppns/{ppn}/status")
    @GET
    public Response getProcessStatusForPPNAsJson(@PathParam("ppn") String ppn) {
        Response response = null;

        List<Integer> ids = null;
        try {
            ids = RestDbHelper.getProcessIdsForIdentifier(ppn);
        } catch (SQLException e) {

        }
        ProcessStatusResponse processStatusResponse = new ProcessStatusResponse();

        if (ids == null || ids.isEmpty()) {
            processStatusResponse.setResult("No proccess with identifier " + ppn + " found");
            response = Response.status(Response.Status.NOT_FOUND).entity(processStatusResponse).build();
        } else if (ids.size() > 1) {
            // TODO
            processStatusResponse.setResult("Found more than one process with identifier " + ppn);
            response = Response.status(Response.Status.CONFLICT).entity(processStatusResponse).build();
        } else {
            Process process = ProcessManager.getProcessById(ids.get(0));

            createResponse(process, processStatusResponse);
            response = Response.status(Response.Status.OK).entity(processStatusResponse).build();
        }

        return response;
    }

    private void createResponse(Process p, ProcessStatusResponse resp) {
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

    private Fileformat getRecordFromCatalogue(Prefs prefs, String identifier, String opacName, String searchField) throws ImportPluginException {
        ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(opacName);
        if (coc == null) {
            throw new ImportPluginException("Catalogue with name " + opacName + " not found. Please check goobi_opac.xml");
        }
        IOpacPlugin myImportOpac = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, coc.getOpacType());
        if (myImportOpac == null) {
            throw new ImportPluginException("Opac plugin " + coc.getOpacType() + " not found. Abort.");
        }
        Fileformat myRdf = null;
        try {
            myRdf = myImportOpac.search(searchField, identifier, coc, prefs);
            if (myRdf == null) {
                throw new ImportPluginException("Could not import record " + identifier
                        + ". Usually this means a ruleset mapping is not correct or the record can not be found in the catalogue.");
            }
        } catch (Exception e1) {
            throw new ImportPluginException("Could not import record " + identifier
                    + ". Usually this means a ruleset mapping is not correct or the record can not be found in the catalogue.");
        }
        DocStruct ds = null;
        DocStruct anchor = null;
        try {
            ds = myRdf.getDigitalDocument().getLogicalDocStruct();
            if (ds.getType().isAnchor()) {
                anchor = ds;
                if (ds.getAllChildren() == null || ds.getAllChildren().isEmpty()) {
                    throw new ImportPluginException(
                            "Could not import record " + identifier + ". Found anchor file, but no children. Try to import the child record.");
                }
                ds = ds.getAllChildren().get(0);
            }
        } catch (PreferencesException e1) {
            throw new ImportPluginException("Could not import record " + identifier
                    + ". Usually this means a ruleset mapping is not correct or the record can not be found in the catalogue.");
        }

        return myRdf;
    }

}
