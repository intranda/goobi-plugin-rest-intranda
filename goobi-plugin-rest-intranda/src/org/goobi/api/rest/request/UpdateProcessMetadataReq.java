package org.goobi.api.rest.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.api.rest.model.RestMetadata;
import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Data;
import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@Data
public class UpdateProcessMetadataReq {
    private List<String> deleteMetadata;
    private Map<String, List<RestMetadata>> addMetadata;

    public UpdateMetadataResponse apply(Process p) throws IOException, InterruptedException, SwapException,
            DAOException, ReadException, PreferencesException, WriteException {
        UpdateMetadataResponse resp = new UpdateMetadataResponse();

        Prefs prefs = p.getRegelsatz().getPreferences();
        Fileformat ff = p.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();

        deleteMetadata(prefs, dd, resp);
        addMetadata(prefs, dd, resp);
        p.writeMetadataFile(ff);

        return resp;
    }

    private void deleteMetadata(Prefs prefs, DigitalDocument dd, UpdateMetadataResponse resp) {
        if (deleteMetadata != null) {
            for (String dm : deleteMetadata) {
                MetadataType mt = prefs.getMetadataTypeByName(dm);
                List<Metadata> allMeta = new ArrayList<>(dd.getLogicalDocStruct().getAllMetadataByType(mt));
                for (Metadata inMeta : allMeta) {
                    if (!dd.getLogicalDocStruct().removeMetadata(inMeta)) {
                        resp.setError(true);
                        resp.addErrorMessage("Can not delete " + dm);
                    }
                }
            }
        }
    }

    private void addMetadata(Prefs prefs, DigitalDocument dd, UpdateMetadataResponse resp) throws IOException,
            InterruptedException, SwapException, DAOException, WriteException, PreferencesException {
        if (addMetadata != null) {
            for (String name : addMetadata.keySet()) {
                try {
                    for (RestMetadata rmd : addMetadata.get(name)) {
                        if (!rmd.anyValue()) {
                            continue;
                        }
                        Metadata md = new Metadata(prefs.getMetadataTypeByName(name));
                        if (rmd.getValue() != null) {
                            md.setValue(rmd.getValue());
                        }
                        if (rmd.getAuthorityID() != null) {
                            md.setAuthorityID(rmd.getAuthorityID());
                        }
                        if (rmd.getAuthorityURI() != null) {
                            md.setAuthorityURI(rmd.getAuthorityURI());
                        }
                        if (rmd.getAuthorityValue() != null) {
                            md.setAuthorityValue(rmd.getAuthorityValue());
                        }
                        dd.getLogicalDocStruct().addMetadata(md);
                    }
                } catch (MetadataTypeNotAllowedException e) {
                    resp.setError(true);
                    resp.addErrorMessage("Metadata not allowed: " + name);
                }
            }
        }
    }
}
