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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Path("/process")
@Log4j
public class CommandImageDownload {

    @Context
    UriInfo uriInfo;

    @Path("download/id/{processId}")
    @GET
    @Produces("application/zip")
    public Response download(@PathParam("processId") int processId) {
        org.goobi.beans.Process process = ProcessManager.getProcessById(processId);
        return startDownload(process);

    }

    @Path("download/title/{processTitle}")
    @GET
    @Produces("application/zip")
    public Response download(@PathParam("processTitle") String processTitle) {
        org.goobi.beans.Process process = ProcessManager.getProcessByTitle(processTitle);
        return startDownload(process);

    }

    // TODO get rid of temporary file
    private Response startDownload(org.goobi.beans.Process process) {
        if (process == null) {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);
            return response.build();
        }
        List<java.nio.file.Path> images = null;
        try {
            images = StorageProvider.getInstance().listFiles(process.getImagesTifDirectory(true));
        } catch (IOException  | SwapException  e1) {
            log.error(e1);
        }

        if (images.isEmpty()) {
            ResponseBuilder response = Response.status(Status.NO_CONTENT);
            return response.build();
        }

        final String zipFile = "/tmp/" + process.getTitel() + ".zip";
        try {
            zipFiles(images, Paths.get(zipFile).toFile());

        } catch (IOException e) {
            log.error(e);
        }

        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (FileInputStream input = new FileInputStream(new File(zipFile))) {
                    try {
                        int bytes;
                        while ((bytes = input.read()) != -1) {
                            output.write(bytes);
                        }
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    }
                }
            }
        };

        return Response.ok(fileStream, "application/zip")
                .header("content-disposition", "attachment; filename = " + process.getTitel() + ".zip")
                .build();
    }

    private static byte[] zipFiles(List<java.nio.file.Path> sourceFiles, File zipFile) throws IOException {

        MessageDigest checksum = null;

        if (zipFile == null || sourceFiles == null || sourceFiles.size() == 0) {
            return null;
        }

        zipFile.getParentFile().mkdirs();

        ZipOutputStream zos = null;
        try {
            FileOutputStream fos = new FileOutputStream(zipFile, true);
            try {
                checksum = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                log.error("No checksum algorithm \"MD5\". Disabling checksum creation");
                checksum = null;
            }
            zos = new ZipOutputStream(fos);
            for (java.nio.file.Path file : sourceFiles) {
                log.debug("Adding file " + file.toString() + " to zipfile " + zipFile.getAbsolutePath());
                zipFile(file, "", zos, checksum);
            }
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
        if (checksum == null) {
            return null;
        }
        return checksum.digest();
    }

    private static void zipFile(java.nio.file.Path file, String path, ZipOutputStream zos, MessageDigest checksum) throws IOException {

        if (file == null) {
            log.error("Attempting to add nonexisting file to zip archive. Ignoring entry.");
            return;
        }

        if (Files.isRegularFile(file)) {

            try (InputStream fis = Files.newInputStream(file)) {

                BufferedInputStream bis = new BufferedInputStream(fis);

                zos.putNextEntry(new ZipEntry(path + file.getFileName()));
                int size;
                byte[] buffer = new byte[2048];
                while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, size);
                    if (checksum != null && size > 0) {
                        checksum.update(buffer, 0, size);
                    }
                }

                bis.close();

            } catch (IOException e) {
                log.error(e);
            }

        } else if (Files.isDirectory(file)) {
            ZipEntry entry = new ZipEntry(path + file.getFileName() + File.separator);
            zos.putNextEntry(entry);

            zos.closeEntry();

            List<java.nio.file.Path> subfiles = StorageProvider.getInstance().listFiles(file.toString());

            if (!subfiles.isEmpty()) {
                for (java.nio.file.Path subFile : subfiles) {
                    zipFile(subFile, path + file.getFileName() + File.separator, zos, checksum);
                }
            }
        } else {
            log.warn("File " + file.toString() + " doesn't seem to exist and cannot be added to zip archive.");
        }
    }
}
