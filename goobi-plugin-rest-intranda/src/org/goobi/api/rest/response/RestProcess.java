package org.goobi.api.rest.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestProcess {
    private int id;
    private String name;
    private Map<String, List<String>> metadata;

    public RestProcess(int id) {
        this.id = id;
        this.metadata = new TreeMap<>();
    }

    public void addMetadata(String name, String value) {
        List<String> values = this.metadata.get(name);
        if (values == null) {
            values = new ArrayList<>();
            this.metadata.put(name, values);
        }
        values.add(value);
    }
}
