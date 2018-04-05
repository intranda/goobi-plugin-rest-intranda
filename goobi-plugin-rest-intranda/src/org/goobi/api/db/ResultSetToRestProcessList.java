package org.goobi.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.api.rest.response.RestProcess;

public class ResultSetToRestProcessList implements ResultSetHandler<List<RestProcess>> {
    private SearchRequest req;

    public ResultSetToRestProcessList(SearchRequest req) {
        this.req = req;
    }

    @Override
    public List<RestProcess> handle(ResultSet rs) throws SQLException {
        Map<Integer, RestProcess> resultMap = new HashMap<>();
        while (rs.next()) {
            Integer id = rs.getInt("processid");
            RestProcess p = resultMap.get(id);
            if (p == null) {
                p = new RestProcess(id);
                p.setName(rs.getString("Titel"));
                resultMap.put(id, p);
            }
            String name = rs.getString("name");
            if (req.getWantedFields() == null || req.getWantedFields().contains(name)) {
                p.addMetadata(name, rs.getString("value"));
            }
        }
        return new ArrayList<RestProcess>(resultMap.values());
    }

}
