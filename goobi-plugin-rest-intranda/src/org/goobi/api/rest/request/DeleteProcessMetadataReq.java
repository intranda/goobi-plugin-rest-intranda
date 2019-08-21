package org.goobi.api.rest.request;

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

    public UpdateMetadataResponse apply(Process p)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
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
