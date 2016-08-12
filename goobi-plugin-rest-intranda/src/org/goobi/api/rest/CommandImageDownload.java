package org.goobi.api.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Path("/process")
@Log4j
public class CommandImageDownload {

    @Context
    UriInfo uriInfo;

    @Path("download/{processid}")
    @GET
    @Produces("application/zip")
    // TODO get rid of temporary file
    public Response download(@PathParam("processid") int processid) {

        org.goobi.beans.Process process = ProcessManager.getProcessById(processid);
        if (process == null) {
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);
            return response.build();
        }
        List<java.nio.file.Path> images = null;
        try {
            images = NIOFileUtils.listFiles(process.getImagesTifDirectory(true));
        } catch (IOException | InterruptedException | SwapException | DAOException e1) {
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
                FileInputStream input = new FileInputStream(new File(zipFile));

                try {
                    int bytes;
                    while ((bytes = input.read()) != -1) {
                        output.write(bytes);
                    }
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                } finally {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                }
            }
        };

        return Response.ok(fileStream, "application/zip").header("content-disposition", "attachment; filename = " + process.getTitel() + ".zip")
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
                if (bis != null) {
                    bis.close();
                }

            } catch (IOException e) {
                log.error(e);
            }

        } else if (Files.isDirectory(file)) {
            ZipEntry entry = new ZipEntry(path + file.getFileName() + File.separator);
            zos.putNextEntry(entry);
            if (zos != null && entry != null) {
                zos.closeEntry();
            }
            List<java.nio.file.Path> subfiles = NIOFileUtils.listFiles(file.toString());

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
