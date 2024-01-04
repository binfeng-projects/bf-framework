package org.bf.framework.boot.support.cache;

import java.util.Map;

public class LocalCacheProperties {
    private String type;
    private String spec;
    private long expireAfterWrite;
    private int initialCapacity;
    private long maximumSize;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getSpec() {
        return spec;
    }
    public void setSpec(String spec) {
        this.spec = spec;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public static LocalCacheProperties newConfig(Map<String,Object> map){

        return newInstance();
    }
    public static LocalCacheProperties newInstance(){
        return new LocalCacheProperties();
    }
}
