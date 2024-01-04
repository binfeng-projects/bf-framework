package org.bf.framework.autoconfigure.hive.support;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HiveTemplate implements ResourceLoaderAware {

	private HiveClient client;
	private ResourceLoader resourceLoader;

	public HiveTemplate(HiveClient client) {
		this.client = client;
	}

	public <T> T execute(HiveClientCallback<T> action) throws DataAccessException {
		Assert.notNull(action, "a valid callback is required");
		try {
			return action.doInHive(client);
		} catch (Exception ex) {
			throw convertHiveAccessException(ex);
		} finally {
			try {
				client.shutdown();
			} catch (Exception ex) {
				throw new InvalidDataAccessResourceUsageException("Error while closing client connection", ex);
			}
		}
	}

	protected DataAccessException convertHiveAccessException(Exception ex) {
		return HiveUtils.convert(ex);
	}

	public List<String> query(String query) throws DataAccessException {
		return query(query, null);
	}

	public List<String> query(String query, Map<?, ?> arguments) throws DataAccessException {
		Assert.hasText(query, "a script is required");

		Resource res = null;

		if (ResourceUtils.isUrl(query)) {
			if (resourceLoader != null) {
				res = resourceLoader.getResource(query);
			}
		}
		else {
			res = new ByteArrayResource(query.getBytes());
		}
		return executeScript(new HiveScript(res, arguments));
	}

	public String queryForString(String query) throws DataAccessException {
		return queryForString(query, null);
	}

	public String queryForString(String query, Map<?, ?> arguments) throws DataAccessException {
		return DataAccessUtils.singleResult(query(query, arguments));
	}

	public Integer queryForInt(String query) throws DataAccessException {
		return queryForInt(query, null);
	}

	public Integer queryForInt(String query, Map<?, ?> arguments) throws DataAccessException {
		String result = queryForString(query, arguments);
		if (result != null) {
			try {
				return Integer.valueOf(result);
			} catch (NumberFormatException ex) {
				throw new TypeMismatchDataAccessException("Invalid int result found [" + result + "]", ex);
			}
		}
		return null;
	}

	public Long queryForLong(String query) throws DataAccessException {
		return queryForLong(query, null);
	}
	public Long queryForLong(String query, Map<?, ?> arguments) throws DataAccessException {
		String result = queryForString(query, arguments);
		if (result != null) {
			try {
				return Long.valueOf(result);
			} catch (NumberFormatException ex) {
				throw new TypeMismatchDataAccessException("Invalid long result found [" + result + "]", ex);
			}
		}
		return null;
	}

	public List<String> executeScript(HiveScript script) throws DataAccessException {
		return executeScript(Collections.singleton(script));
	}
	public List<String> executeScript(final Iterable<HiveScript> scripts) throws DataAccessException {
		return execute(new HiveClientCallback<List<String>>() {
			@Override
			public List<String> doInHive(HiveClient hiveClient) throws Exception {
				return HiveUtils.run(hiveClient, scripts);
			}
		});
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}