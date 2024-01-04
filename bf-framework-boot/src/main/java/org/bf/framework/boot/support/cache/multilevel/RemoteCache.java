package org.bf.framework.boot.support.cache.multilevel;

import org.bf.framework.boot.util.CacheUitl;
import org.bf.framework.common.util.CollectionUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;

public class RemoteCache extends AbstractValueAdaptingCache {
    protected Collection<Cache> caches;
    public RemoteCache(Collection<Cache> caches) {
        super(true);
        if(CollectionUtils.isEmpty(caches)) {
            throw new RuntimeException("caches empty");
        }
        this.caches = caches;
    }

    public RemoteCache(String prefix,String schema,String... cacheNames) {
        super(true);
        this.caches = CacheUitl.getCachesFromCacheManager(prefix,schema,CollectionUtils.newHashSet(cacheNames));
        if(CollectionUtils.isEmpty(caches)) {
            throw new RuntimeException("caches empty");
        }
    }

    @Override
    public String getName() {
        return "RemoteCache";

    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper result = get(key);
        return result != null ? (T) result.get() : getSynchronized(key, valueLoader);
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> T getSynchronized(Object key, Callable<T> valueLoader) {
        ValueWrapper result = get(key);
        return result != null ? (T) result.get() : loadCacheValue(key, valueLoader);
    }

    protected <T> T loadCacheValue(Object key, Callable<T> valueLoader) {
        T value;
        try {
            value = valueLoader.call();
        }
        catch (Exception cause) {
            throw new ValueRetrievalException(key, valueLoader, cause);
        }
        put(key, value);
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        for (Cache c : caches) {
            c.put(key, value);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper valueWrapper = null;
        for (Cache c : caches) {
            valueWrapper = c.putIfAbsent(key, value);
        }
        return valueWrapper;
    }


    @Override
    public void evict(Object key) {
        for (Cache c : caches) {
            c.evict(key);
        }
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean result = false;
        for (Cache c : caches) {
            result = c.evictIfPresent(key);
        }
        return result;
    }

    @Override
    public void clear() {
        for (Cache c : caches) {
            c.clear();
        }
    }

    @Override
    protected Object lookup(Object key) {
        Assert.notNull(key, "key cannot be empty");
        ValueWrapper value;
        for (Cache c : caches) {
            value = c.get(key);
            if (Objects.nonNull(value)) {
                return value.get();
            }
        }
        return null;
    }
}