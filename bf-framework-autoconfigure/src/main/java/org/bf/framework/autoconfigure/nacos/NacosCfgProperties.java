package org.bf.framework.autoconfigure.nacos;

import org.bf.framework.boot.constant.MiddlewareConst;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MiddlewareConst.PREFIX_NACOS)
public class NacosCfgProperties {
    private String serverAddr;
    private String accessKey;
    private String secretKey;
    private String username;
    private String password;
    private String endpoint;
    private String contextPath;
    private String configLongPollTimeout;

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

    public String getConfigLongPollTimeout() {
        return configLongPollTimeout;
    }

    public void setConfigLongPollTimeout(String configLongPollTimeout) {
        this.configLongPollTimeout = configLongPollTimeout;
    }

    public String getConfigRetryTime() {
        return configRetryTime;
    }

    public void setConfigRetryTime(String configRetryTime) {
        this.configRetryTime = configRetryTime;
    }

    public String getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(String maxRetry) {
        this.maxRetry = maxRetry;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDiscoveryNamespace() {
        return discoveryNamespace;
    }

    public void setDiscoveryNamespace(String discoveryNamespace) {
        this.discoveryNamespace = discoveryNamespace;
    }

    public String getDiscoveryContextPath() {
        return discoveryContextPath;
    }

    public void setDiscoveryContextPath(String discoveryContextPath) {
        this.discoveryContextPath = discoveryContextPath;
    }

    private String configRetryTime;
    private String maxRetry;
    private String encode;
    private String clusterName;
    private String namespace;
    private String discoveryNamespace;
    private String discoveryContextPath;

    public NacosCfgProperties() {
    }

    public String getServerAddr() {
        return this.serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getContextPath() {
        return this.contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getEncode() {
        return this.encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
