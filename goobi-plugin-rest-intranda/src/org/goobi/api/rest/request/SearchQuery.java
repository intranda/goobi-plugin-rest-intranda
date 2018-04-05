package org.goobi.api.rest.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchQuery {
    private String field;
    private String value;
    private RelationalOperator relation;

    public void createSqlClause(StringBuilder b) {
        b.append('(');
        b.append("name=? and value");
        b.append(relation.toString());
        b.append("?");
        b.append(')');
    }

    public enum RelationalOperator {
        EQUAL("="),
        LESS("<"),
        GT(">"),
        LESSEQ("<="),
        GTEQ(">="),
        NEQUAL("!=");

        private final String sqlStr;

        private RelationalOperator(String value) {
            this.sqlStr = value;
        }

        @Override
        public String toString() {
            return sqlStr;
        }

    }

    public void addParams(List<Object> params) {
        params.add(field);
        params.add(value);
    };
}
