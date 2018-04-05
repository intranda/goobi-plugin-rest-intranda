package org.goobi.api.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.api.rest.response.RestProcess;

import de.sub.goobi.persistence.managers.MySQLHelper;

public class RestDbHelper {

    public static List<RestProcess> searchProcesses(SearchRequest req) throws SQLException {
    	List<RestProcess> results = null;
    	String sql = req.createSql();
    	Object[] params = req.createSqlParams();
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            QueryRunner run = new QueryRunner();
            results = run.query(conn, sql, new ResultSetToRestProcessList(req), params);
        }
        //TODO: maybe filter a little (?)
        return results;
    }
}
