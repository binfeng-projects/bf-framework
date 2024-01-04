package org.bf.framework.boot.support.cache.sync;


import java.io.Serializable;
import java.util.Collection;

public class SyncCacheProperties implements Serializable {
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private String cacheManager;
    private Collection<String> cacheNames;
    private String key;
    private Object value;

    private String prefix;

    private String schema;
    public String getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(String cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Collection<String> getCacheNames() {
        return cacheNames;
    }

    public void setCacheNames(Collection<String> cacheNames) {
        this.cacheNames = cacheNames;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
