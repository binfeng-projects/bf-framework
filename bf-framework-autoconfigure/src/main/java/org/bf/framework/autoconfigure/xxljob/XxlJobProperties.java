package org.bf.framework.autoconfigure.xxljob;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bf.xxl")
public class XxlJobProperties {

    private String accessToken;

    private Executor executor;

    private Admin admin;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public static class Executor {

        private String appname;

        private String ip;

        private int port;

        private String logpath;

        private int logretentiondays;

        public void setAppname(String appname) {
            this.appname = appname;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setLogpath(String logpath) {
            this.logpath = logpath;
        }

        public void setLogretentiondays(int logretentiondays) {
            this.logretentiondays = logretentiondays;
        }

        public String getAppname() {
            return appname;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public String getLogpath() {
            return logpath;
        }

        public int getLogretentiondays() {
            return logretentiondays;
        }
    }

    public static class Admin {

        private String addresses;

        public String getAddresses() {
            return addresses;
        }

        public void setAddresses(String addresses) {
            this.addresses = addresses;
        }
    }
}
