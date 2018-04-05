package org.goobi.api.rest.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class SearchRequest {
    private List<SearchGroup> metadataFilters = new ArrayList<>();
    private boolean metadataConjunctive;
    private String filterProject;
    private String filterObjectType;
    private String filterStep;
    private String structureType;

    private Set<String> wantedFields;

    private String sortField;
    private boolean sortDescending;

    private int limit;
    private int offset;

    public void addSearchGroup(SearchGroup group) {
        this.metadataFilters.add(group);
    }

    public String createSql() {
        //example sql: select * from metadata left join prozesse on metadata.processid = prozesse.ProzesseID where prozesse.ProzesseID IN 
        //(select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018");
        StringBuilder builder = new StringBuilder();
        createSelect(builder);
        createFrom(builder);
        createWhere(builder);
        return builder.toString();
    }

    private void createSelect(StringBuilder b) {
        b.append("SELECT * ");
    }

    private void createFrom(StringBuilder b) {
        b.append("FROM metadata LEFT JOIN prozesse ON metadata.processid = prozesse.ProzesseID ");
    }

    private void createWhere(StringBuilder b) {
        String conj = metadataConjunctive ? "AND " : "OR ";
        b.append("WHERE prozesse.ProzesseID IN ( SELECT * FROM ( SELECT processid FROM metadata WHERE (");
        for (int i = 0; i < metadataFilters.size(); i++) {
            SearchGroup sg = metadataFilters.get(i);
            sg.createSqlClause(b);
            if (i + 1 < metadataFilters.size()) {
                b.append(conj);
            }
        }
        b.append(") ORDER BY processid ASC ");
        if (limit != 0) {
            b.append("LIMIT ? OFFSET ? ");
        }
        b.append(") as t) ");
    }

    public Object[] createSqlParams() {
        List<Object> params = new ArrayList<>();
        addSelectParams(params);
        addFromParams(params);
        addWhereParams(params);
        Object[] paramsArr = new Object[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
    }

    private void addSelectParams(List<Object> params) {
        // nothing to do here at the moment
    }

    private void addFromParams(List<Object> params) {
        // nothing to do here at the moment
    }

    private void addWhereParams(List<Object> params) {
        for (SearchGroup sg : metadataFilters) {
            sg.addParams(params);
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }
}
