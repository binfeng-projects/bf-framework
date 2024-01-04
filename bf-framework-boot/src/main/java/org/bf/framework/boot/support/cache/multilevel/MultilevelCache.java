package org.bf.framework.boot.support.cache.multilevel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.Callable;

@Slf4j
public class MultilevelCache extends AbstractValueAdaptingCache {
    private RemoteCache remoteCache;
    private LocalCache localCache;
    public MultilevelCache(LocalCache localCache,RemoteCache remoteCache) {
        super(true);
        if(remoteCache == null) {
            throw new RuntimeException("remoteCache cannot be null");
        }
        if(localCache == null) {
            throw new RuntimeException("localCache cannot be null");
        }
        this.remoteCache = remoteCache;
        this.localCache = localCache;
    }
    @Override
    public String getName() {
        return "MultilevelCache";

    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        T result = localCache.get(key,valueLoader);
        if (result == null) {
            result = remoteCache.get(key,valueLoader);
        }
        return result;
    }

    @Override
    public void put(Object key, Object value) {
        remoteCache.put(key, value);
        localCache.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        remoteCache.putIfAbsent(key, value);
        return localCache.putIfAbsent(key, value);
    }


    @Override
    public void evict(Object key) {
        remoteCache.evict(key);
        localCache.evict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        remoteCache.evictIfPresent(key);
        return localCache.evictIfPresent(key);
    }

    @Override
    public void clear() {
        remoteCache.clear();
        localCache.clear();
    }

    @Override
    protected Object lookup(Object key) {
        Assert.notNull(key, "key cannot be empty");
        ValueWrapper value;
        value = localCache.get(key);
        if (Objects.nonNull(value)) {
            return value.get();
        }
        value = remoteCache.get(key);
        if (Objects.nonNull(value)) {
            ValueWrapper finalValue = value;
            localCache.put(key, finalValue.get());
            return value.get();
        }
        return null;
    }
}