package org.goobi.api.rest.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchQuery {
    private String field;
    private String value;
}
