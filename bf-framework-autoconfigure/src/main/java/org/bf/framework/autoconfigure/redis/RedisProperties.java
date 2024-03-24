package org.bf.framework.autoconfigure.redis;

import java.time.Duration;
import java.util.List;

public class RedisProperties {
	private int database = 0;

	/**
	 * host:port
	 */
	private String url;
	private String host;
	private String username;
	private String password;
	private int port;
	private Duration timeout;
	private Duration connectTimeout;
	private String type;
	private TopicListener topicListener;
	/**
	 * 是否启用redisson
 	 */
	private boolean useRedisson;
	private org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool pool;
	private org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel sentinel;
	private org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster cluster;
	private final org.springframework.boot.autoconfigure.data.redis.RedisProperties.Ssl ssl = new org.springframework.boot.autoconfigure.data.redis.RedisProperties.Ssl();
	private final Lettuce lettuce = new Lettuce();
	public static class Lettuce {
		private Duration shutdownTimeout = Duration.ofMillis(100);
		private boolean dynamicRefreshSources = true;

		private Duration period;

		private boolean adaptive;
		public Duration getShutdownTimeout() {
			return shutdownTimeout;
		}

		public void setShutdownTimeout(Duration shutdownTimeout) {
			this.shutdownTimeout = shutdownTimeout;
		}

		public boolean isDynamicRefreshSources() {
			return dynamicRefreshSources;
		}

		public void setDynamicRefreshSources(boolean dynamicRefreshSources) {
			this.dynamicRefreshSources = dynamicRefreshSources;
		}

		public Duration getPeriod() {
			return period;
		}

		public void setPeriod(Duration period) {
			this.period = period;
		}

		public boolean isAdaptive() {
			return adaptive;
		}

		public void setAdaptive(boolean adaptive) {
			this.adaptive = adaptive;
		}

	}
	public static class TopicListener {
		/**
		 * 订阅的topic，如果有值，会创建监听
		 */
		private List<String> topics;
		/**
		 * 监听器，类全名
		 */
		private String listener;

		public List<String> getTopics() {
			return topics;
		}

		public void setTopics(List<String> topics) {
			this.topics = topics;
		}

		public String getListener() {
			return listener;
		}

		public void setListener(String listener) {
			this.listener = listener;
		}

	}
	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Duration getTimeout() {
		return timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	public Duration getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public TopicListener getTopicListener() {
		return topicListener;
	}

	public void setTopicListener(TopicListener topicListener) {
		this.topicListener = topicListener;
	}

	public boolean isUseRedisson() {
		return useRedisson;
	}

	public void setUseRedisson(boolean useRedisson) {
		this.useRedisson = useRedisson;
	}

	public org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool getPool() {
		return pool;
	}

	public void setPool(org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool pool) {
		this.pool = pool;
	}

	public org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel getSentinel() {
		return sentinel;
	}

	public void setSentinel(org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel sentinel) {
		this.sentinel = sentinel;
	}

	public org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster getCluster() {
		return cluster;
	}

	public void setCluster(org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster cluster) {
		this.cluster = cluster;
	}

	public org.springframework.boot.autoconfigure.data.redis.RedisProperties.Ssl getSsl() {
		return ssl;
	}

	public Lettuce getLettuce() {
		return lettuce;
	}

}
