package org.goobi.api.rest.request;

import java.util.List;

import lombok.Data;

@Data
public class SearchRequest {
    private List<SearchGroup> metadataFilters;
    private boolean metadataConjunctive;
    private String filterProject;
    private String filterObjectType;
    private String filterStep;
    private String structureType;

    private String sortField;
    private String sortOrder;
}
