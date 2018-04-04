package org.goobi.api.rest.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestProcess {
    private int id;
    private String name;
    private Map<String, String> metadata;
}
