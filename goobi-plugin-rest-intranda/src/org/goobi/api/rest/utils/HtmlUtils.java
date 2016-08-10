package org.goobi.api.rest.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.goobi.api.rest.response.ProcessStatusResponse;
import org.goobi.api.rest.response.StepResponse;

@Provider
@Produces(MediaType.TEXT_HTML)
public class HtmlUtils implements MessageBodyWriter<ProcessStatusResponse> {

    @Override
    public long getSize(ProcessStatusResponse arg0, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ProcessStatusResponse.class;
    }

    @Override
    public void writeTo(ProcessStatusResponse resp, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException, WebApplicationException {

        Writer writer = new PrintWriter(out);
        writer.write("<html>");
        writer.write("<body>");
        writer.write("<h2>" + resp.getTitle() + "</h2>");
        writer.write("<div>Id: " + resp.getId() + "</div>");
        writer.write("<div>Title: " + resp.getTitle() + "</div>");
        writer.write("<div>Creation date: " + resp.getCreationDate() + "</div>");
        writer.write("<div><div>Steps:</div>");
        for (StepResponse sr : resp.getStep()) {

            writer.write("<div>");
            writer.write("<div>Step title: " + sr.getTitle() + "</div>");
            writer.write("<div>Step status: " + sr.getStatus() + "</div>");
            writer.write("<div>User: " + sr.getUser() + "</div>");
            writer.write("<div>Start date: " + sr.getStartDate() + "</div>");
            writer.write("<div>End date: " + sr.getEndDate() + "</div>");

            writer.write("</div>");
        }

        writer.write("</div>");
        writer.write("</body>");
        writer.write("</html>");

        writer.flush();
        writer.close();

    }

}
