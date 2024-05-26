package org.bf.framework.autoconfigure.sentinel;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_SENTINEL;

@ConfigurationProperties(prefix = PREFIX_SENTINEL)
public class SentinelProperties {
    private String dataSourceType;
    private String dataSourceRef;
    private String namespace;
    private String appId;
    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getDataSourceRef() {
        return dataSourceRef;
    }

    public void setDataSourceRef(String dataSourceRef) {
        this.dataSourceRef = dataSourceRef;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
