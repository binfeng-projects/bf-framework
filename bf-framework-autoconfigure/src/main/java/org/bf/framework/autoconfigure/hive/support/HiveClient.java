package org.bf.framework.autoconfigure.hive.support;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HiveClient {

	private JdbcTemplate jdbcTemplate;

	public HiveClient(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public Connection getConnection() {
		return DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
	}

	public String executeAndfetchOne(final String command) {
		List<String> results = execute(command);
		if (results.size() < 1) {
			throw new IncorrectResultSizeDataAccessException(1);
		}
		return results.get(0);
	}

	public List<String> execute(final String command) {
		return jdbcTemplate.execute(new ConnectionCallback<List<String>>() {
			@Override
			public List<String> doInConnection(Connection con) throws SQLException, DataAccessException {
				List<String> results = new ArrayList<String>();
				Statement stmt = con.createStatement();
				ResultSet rs = null;
				int i = 0;
				try {
					boolean retRs = stmt.execute(command);
					if (retRs) {
						rs = stmt.getResultSet();
						while (rs.next()) {
							results.add(rs.getString(1));
							i++;
						}
					}
				}
				finally {
					if (rs != null) {
						try {
							rs.close();
						}
						catch (Exception ignore) {}
					}
					if (stmt != null) {
						try {
							stmt.close();
						}
						catch (Exception ignore) {}
					}
				}
				return results;
			}
		});
	}

	JdbcOperations getJdbcOperations() {
		return jdbcTemplate;
	}

	public void shutdown() throws SQLException {
		jdbcTemplate.getDataSource().getConnection().close();
	}
}
