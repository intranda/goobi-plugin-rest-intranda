package org.goobi.api.rest.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SearchRequest {
    private List<SearchGroup> metadataFilters = new ArrayList<>();
    private boolean metadataConjunctive;
    private String filterProject;
    private String filterObjectType;
    private String filterStep;
    private String structureType;

    private List<String> wantedFields;
    
    private String sortField;
    private String sortOrder;
    
    private int limit;
    private int offset;
    
    public void addSearchGroup(SearchGroup group) {
    	this.metadataFilters.add(group);
    }

	public String createSql() {
		//example sql: select * from metadata left join prozesse on metadata.processid = prozesse.ProzesseID where prozesse.ProzesseID IN (select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018");
		String conj = metadataConjunctive ? "AND" : "OR";
		StringBuilder builder = new StringBuilder();
		createSelect(builder);
		createFrom(builder);
		createWhere(builder);
		createSortOrderAndLimit(builder);
		return builder.toString();
	}

	private void createSelect(StringBuilder b) {
		b.append("SELECT * ");
	}

	private void createFrom(StringBuilder b) {
		
		b.append("FROM metadata LEFT JOIN prozesse USING prozesseid");
		
	}

	private void createWhere(StringBuilder b) {
		// TODO Auto-generated method stub
		
	}

	private void createSortOrderAndLimit(StringBuilder b) {
		// TODO Auto-generated method stub
		
	}

	public Object[] createSqlParams() {
		// TODO Auto-generated method stub
		return null;
	}
}
