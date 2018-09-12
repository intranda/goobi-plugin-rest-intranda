package org.goobi.api.rest.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class DeleteProcessMetadataReq {
    private List<String> deleteMetadata;

    public UpdateMetadataResponse apply(Process p) throws ReadException, PreferencesException, WriteException, IOException, InterruptedException,
            SwapException, DAOException {
        UpdateMetadataResponse resp = new UpdateMetadataResponse();
        Prefs prefs = p.getRegelsatz().getPreferences();
        Fileformat ff = p.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();
        deleteMetadata(prefs, dd, resp);

        return resp;
    }

    private void deleteMetadata(Prefs prefs, DigitalDocument dd, UpdateMetadataResponse resp) {
        Set<MetadataGroup> delGroups = new HashSet<>();
        if (deleteMetadata != null) {
            for (String name : deleteMetadata) {
                if (name.contains("/")) {
                    //handle group
                    String[] split = name.split("/");
                    String group = split[0];
                    String metaName = split[1];
                    MetadataGroupType mgt = prefs.getMetadataGroupTypeByName(group);
                    List<MetadataGroup> groups = dd.getLogicalDocStruct().getAllMetadataGroupsByType(mgt);
                    delGroups.addAll(groups);
                    for (MetadataGroup delGroup : groups) {
                        List<Metadata> allMeta = new ArrayList<>(delGroup.getMetadataByType(metaName));
                        for (Metadata inMeta : allMeta) {
                            if (!dd.getLogicalDocStruct().removeMetadata(inMeta)) {
                                resp.setError(true);
                                resp.addErrorMessage("Can not delete " + name);
                            }
                        }
                    }
                } else {
                    MetadataType mt = prefs.getMetadataTypeByName(name);
                    List<Metadata> allMeta = new ArrayList<>(dd.getLogicalDocStruct().getAllMetadataByType(mt));
                    for (Metadata inMeta : allMeta) {
                        if (!dd.getLogicalDocStruct().removeMetadata(inMeta)) {
                            resp.setError(true);
                            resp.addErrorMessage("Can not delete " + name);
                        }
                    }
                }
            }
        }
    }
}
