package org.goobi.api.rest.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SearchGroup {
    private List<SearchQuery> filters;
    private boolean conjunctive;
    
    public SearchGroup() {
    	this.filters = new ArrayList<>();
    }
    
    public void addFilter(SearchQuery query) {
    	this.filters.add(query);
    }
}
