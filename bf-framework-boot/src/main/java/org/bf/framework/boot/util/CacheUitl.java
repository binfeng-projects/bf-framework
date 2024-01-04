package org.bf.framework.boot.util;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.support.cache.sync.SyncCacheProperties;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.JSON;
import org.bf.framework.common.util.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.List;

@Slf4j
public class CacheUitl {
    public static List<Cache> getCachesFromCacheManager(SyncCacheProperties properties){
        List<Cache> caches = CollectionUtils.newArrayList();
        if(null == properties || CollectionUtils.isEmpty(properties.getCacheNames())){
            return caches;
        }
        if(StringUtils.isNotBlank(properties.getCacheManager())) {
            return getCachesFromCacheManager(properties.getCacheManager(),properties.getCacheNames());
        }
        return getCachesFromCacheManager(properties.getPrefix(),properties.getSchema(),properties.getCacheNames());
    }
    public static List<Cache> getCachesFromCacheManager(String prefix,String schemaName,Collection<String> cacheNames){
        List<Cache> caches = CollectionUtils.newArrayList();
        if(CollectionUtils.isEmpty(cacheNames)){
            return caches;
        }
        CacheManager cm = null;
        if (StringUtils.isBlank(schemaName) || StringUtils.isBlank(prefix)) {
            throw new RuntimeException("schemaName && prefix cannot be null");
        }
        cm = (CacheManager)Middleware.getMiddlewareBean(prefix,schemaName,CacheManager.class);
        if (cm == null) {
            return caches;
        }
        for (String cacheName : cacheNames){
            Cache ch = cm.getCache(cacheName);
            if(null == ch){
                continue;
            }
            caches.add(ch);
        }
        return caches;
    }
    public static List<Cache> getCachesFromCacheManager(String cacheManager,Collection<String> cacheNames){
        List<Cache> caches = CollectionUtils.newArrayList();
        if(CollectionUtils.isEmpty(cacheNames)){
            return caches;
        }
        CacheManager cm = null;
        if (StringUtils.isBlank(cacheManager)) {
            throw new RuntimeException("cacheManager cannot be null");
        }
        cm = SpringUtil.getBean(cacheManager,CacheManager.class);
        if (cm == null) {
            return caches;
        }
        for (String cacheName : cacheNames){
            Cache ch = cm.getCache(cacheName);
            if(null == ch){
                continue;
            }
            caches.add(ch);
        }
        return caches;
    }
    /**
     * 清理本地缓存
     */
    public static boolean syncCache(SyncCacheProperties properties) {
        if(properties == null){
            return false;
        }
        List<Cache> caches = getCachesFromCacheManager(properties);
        if(CollectionUtils.isEmpty(caches)){
            return false;
        }
        for (Cache c : caches){
            try {
                if(StringUtils.isBlank(properties.getKey())) { //key为空表示清空
                    c.clear();
                } else if (properties.getValue() == null) { //value等于null表示删除这个key
                    boolean actionResult = c.evictIfPresent(properties.getKey());
                    log.info("evict cache result : " + actionResult + " : " + JSON.toJSONString(properties));
                } else { //否则表示更新缓存
                    c.put(properties.getKey(), properties.getValue());
                }
            }catch (Exception e){
                log.error("evict cache error : " + c.getName() + " : " + JSON.toJSONString(properties),e);
            }
        }
        return true;
    }

}
