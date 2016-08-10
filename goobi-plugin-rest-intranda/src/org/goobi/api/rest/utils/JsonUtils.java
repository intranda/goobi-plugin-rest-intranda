package org.goobi.api.rest.utils;

import java.io.StringWriter;

import org.goobi.api.rest.response.ProcessStatusResponse;

import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String jsonFromObject(Object object) {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
        return writer.toString();
    }

    public static Object userFromJson(String json) {
        return objectFromJson(json, ProcessStatusResponse.class);
    }

    static <t> ProcessStatusResponse objectFromJson(String json, Class<t> klass) {
        ProcessStatusResponse object;
        try {
            object = (ProcessStatusResponse) mapper.readValue(json, klass);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
        return object;

    }

}
