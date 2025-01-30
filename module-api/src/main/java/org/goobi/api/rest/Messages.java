package org.goobi.api.rest;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;

@Path("/messages")
public class Messages {

    @GET
    @Path("/{language}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getBundleForLanguage(@PathParam("language") String language) {
        Locale locale = new Locale(language);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        Map<String, String> bundleMap = new HashMap<>();
        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            bundleMap.put(key, bundle.getString(key));
        }
        java.nio.file.Path file = Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages());
        if (StorageProvider.getInstance().isFileExists(file)) {
            // Load local message bundle from file system only if file exists;
            // if value not exists in bundle, use default bundle from classpath

            try {
                final URL resourceURL = file.toUri().toURL();
                URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    @Override
                    public URLClassLoader run() {
                        return new URLClassLoader(new URL[] { resourceURL });
                    }
                });
                ResourceBundle localBundle = ResourceBundle.getBundle("messages", locale, urlLoader);
                if (localBundle != null) {
                    for (Enumeration<String> keys = localBundle.getKeys(); keys.hasMoreElements();) {
                        String key = keys.nextElement();
                        bundleMap.put(key, localBundle.getString(key));
                    }
                }

            } catch (Exception e) {
            }
        }
        return bundleMap;
    }
}
