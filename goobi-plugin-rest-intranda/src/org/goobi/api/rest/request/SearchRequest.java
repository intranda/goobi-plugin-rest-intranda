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
        StringBuilder b = new StringBuilder();
        createySelect(b);
        createFrom(b);
        createWhere(b);
        createOrderAndLimit(b);
        return b.toString();
    }

    private void createySelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID,  metadatenkonfigurationen.Datei ");
    }

    private void createFrom(StringBuilder b) {
        b.append("FROM metadata_json LEFT JOIN prozesse ON metadata_json.processid = prozesse.ProzesseID LEFT JOIN metadatenkonfigurationen on metadatenkonfigurationen.MetadatenKonfigurationID=prozesse.MetadatenKonfigurationID ");
    }

    private void createWhere(StringBuilder b) {
        String conj = metadataConjunctive ? "AND " : "OR ";
        b.append("WHERE ");
        for (int i = 0; i < metadataFilters.size(); i++) {
            SearchGroup sg = metadataFilters.get(i);
            sg.createSqlClause(b);
            if (i + 1 < metadataFilters.size()) {
                b.append(conj);
            }
        }
    }

    private void createOrderAndLimit(StringBuilder b) {
        if (sortField != null && !sortField.isEmpty()) {
            b.append("ORDER BY JSON_EXTRACT(value, ?) ");
            b.append(sortDescending ? "DESC " : "ASC ");
        } else {
            b.append("ORDER BY processid ASC ");
        }
        if (limit != 0) {
            b.append("LIMIT ? OFFSET ?");
        }
    }

    public Object[] createSqlParams() {
        List<Object> params = new ArrayList<>();
        addWhereParams(params);
        addOrderAndLimitParams(params);
        Object[] paramsArr = new Object[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
    }

    private void addWhereParams(List<Object> params) {
        for (SearchGroup sg : metadataFilters) {
            sg.addParams(params);
        }
    }

    private void addOrderAndLimitParams(List<Object> params) {
        if (sortField != null && !sortField.isEmpty()) {
            params.add("$." + sortField);
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }

    public String createLegacySql() {
        //example sql: select * from metadata left join prozesse on metadata.processid = prozesse.ProzesseID where prozesse.ProzesseID IN 
        //(select processid from metadata where metadata.name="_dateDigitization" and metadata.value="2018");
        StringBuilder builder = new StringBuilder();
        createLegacySelect(builder);
        createLegacyFrom(builder);
        createLegacyWhere(builder);
        return builder.toString();
    }

    private void createLegacySelect(StringBuilder b) {
        b.append("SELECT prozesse.ProzesseID,  metadatenkonfigurationen.Datei ");
    }

    private void createLegacyFrom(StringBuilder b) {
        b.append("FROM metadata LEFT JOIN prozesse ON metadata.processid = prozesse.ProzesseID LEFT JOIN metadatenkonfigurationen on metadatenkonfigurationen.MetadatenKonfigurationID=prozesse.MetadatenKonfigurationID ");
    }

    private void createLegacyWhere(StringBuilder b) {
        String conj = metadataConjunctive ? "AND " : "OR ";
        b.append("WHERE prozesse.ProzesseID IN ( SELECT * FROM ( SELECT processid FROM metadata WHERE (");
        for (int i = 0; i < metadataFilters.size(); i++) {
            SearchGroup sg = metadataFilters.get(i);
            sg.createLegacySqlClause(b);
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

    public Object[] createLegacySqlParams() {
        List<Object> params = new ArrayList<>();
        addLegacyWhereParams(params);
        Object[] paramsArr = new Object[params.size()];
        params.toArray(paramsArr);
        return paramsArr;
    }

    private void addLegacyWhereParams(List<Object> params) {
        for (SearchGroup sg : metadataFilters) {
            sg.addLegacyParams(params);
        }
        if (limit != 0) {
            params.add(limit);
            params.add(offset);
        }
    }

}
