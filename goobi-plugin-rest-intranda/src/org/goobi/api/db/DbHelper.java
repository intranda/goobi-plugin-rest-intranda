package org.goobi.api.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.goobi.api.rest.response.RestProcess;

import de.sub.goobi.persistence.managers.MySQLHelper;

public class DbHelper {

    // TODO: do the search
    public static List<RestProcess> searchProcesses() throws SQLException {
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            return null;
        }
    }
}
