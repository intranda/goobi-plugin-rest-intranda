package org.goobi.api.rest.request;

import lombok.Data;

@Data
public class SearchQuery {
    private String field;
    private String value;
}
