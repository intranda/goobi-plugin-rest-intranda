package org.goobi.api.rest.response;

import java.util.Map;
import java.util.TreeMap;

import lombok.Data;

@Data
public class RestProcess {
    private int id;
    private String name;
    private Map<String, String> metadata;

    public RestProcess(int id) {
        this.id = id;
        this.metadata = new TreeMap<>();
    }

    public void addMetadata(String name, String value) {
        this.metadata.put(name, value);
    }
}
