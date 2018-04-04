package org.goobi.api.rest.request;

import java.util.List;

import lombok.Data;

@Data
public class SearchGroup {
    private List<SearchQuery> filters;
    private boolean conjunctive;
}
