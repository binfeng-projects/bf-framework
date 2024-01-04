package org.bf.framework.boot.support.cache.multilevel;

import cn.hutool.core.thread.ThreadUtil;
import lombok.experimental.FieldNameConstants;
import org.bf.framework.boot.support.cache.sync.CacheSync;
import org.bf.framework.boot.support.cache.sync.SyncCacheProperties;
import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

public class LocalCache extends RemoteCache {
    protected Collection<Cache> caches;

    private CacheSync cacheSync;
    Executor syncExecutor = ThreadUtil.newFixedExecutor(Runtime.getRuntime().availableProcessors() * 2, 2000,"multilevelCache-sync",true);
    public LocalCache(CacheSync cacheSync,Collection<Cache> caches) {
        super(caches);
        if(cacheSync == null) {
            throw new RuntimeException("cacheSync cannot be null");
        }
        this.cacheSync = cacheSync;
    }

    public LocalCache(CacheSync cacheSync,String... cacheNames) {
        super(PREFIX_CACHE,SCHEMA_CACHE_LOCAL,cacheNames);
        if(cacheSync == null) {
            throw new RuntimeException("cacheSync cannot be null");
        }
        this.cacheSync = cacheSync;
    }

    @Override
    public String getName() {
        return "LocalCache";

    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        //这个地方和父类实现不同，因为多级缓存一般先查本地，本地没有还会再查分布式缓存
        ValueWrapper result = get(key);
        return result != null ? (T) result.get() : null;
    }

    @Override
    public void put(Object key, Object value) {
        syncLocalCache(key,value);
        super.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        syncLocalCache(key,value);
        return super.putIfAbsent(key, value);
    }


    @Override
    public void evict(Object key) {
        syncLocalCache(key,null);
        super.evict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        syncLocalCache(key,null);
        return super.evictIfPresent(key);
    }

    @Override
    public void clear() {
        syncLocalCache(null,null);
        super.clear();
    }



    @Override
    protected Object lookup(Object key) {
        return super.lookup(key);
    }

    void syncLocalCache(Object key,Object value) {
        syncExecutor.execute(()->{
            SyncCacheProperties properties = new SyncCacheProperties();
            properties.setPrefix(PREFIX_CACHE);
            properties.setSchema(SCHEMA_CACHE_LOCAL);
            properties.setCacheNames(caches.stream().map(Cache::getName).collect(Collectors.toList()));
            properties.setKey((String) key);
            properties.setValue(value);
            cacheSync.syncCache(properties);
        });
    }
}
