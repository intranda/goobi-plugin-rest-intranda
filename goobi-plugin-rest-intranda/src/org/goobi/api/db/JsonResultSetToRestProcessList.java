package org.goobi.api.db;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.rest.response.RestProcess;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonResultSetToRestProcessList implements ResultSetHandler<List<RestProcess>> {
    private static final Gson gson = new Gson();
    private static final Type myMapType = new TypeToken<Map<String, List<String>>>() {
    }.getType();

    @Override
    public List<RestProcess> handle(ResultSet rs) throws SQLException {
        List<RestProcess> resultList = new ArrayList<RestProcess>();
        while (rs.next()) {
            Integer id = rs.getInt("processid");
            String name = rs.getString("Titel");
            Map<String, List<String>> metadata = gson.fromJson(rs.getString("value"), myMapType);
            resultList.add(new RestProcess(id, name, metadata));
        }
        return resultList;
    }

}
