package org.goobi.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
		// TODO Auto-generated method stub
		return null;
	}

}
