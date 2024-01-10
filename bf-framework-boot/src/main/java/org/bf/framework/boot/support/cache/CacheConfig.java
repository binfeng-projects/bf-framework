package org.bf.framework.boot.support.cache;

import org.bf.framework.boot.annotation.condition.OnBean;
import org.bf.framework.boot.annotation.condition.OnMissingBean;
import org.bf.framework.boot.support.cache.multilevel.LocalCache;
import org.bf.framework.boot.support.cache.multilevel.MultilevelCache;
import org.bf.framework.boot.support.cache.multilevel.RemoteCache;
import org.bf.framework.boot.support.cache.sync.CacheSync;
import org.bf.framework.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

//@OnBean(name = {CacheConfig.LOCAL_CACHE_MANAGER, CacheConfig.PRIMARY_CACHE_SYNC})
//@OnBean(name = {CacheConfig.LOCAL_CACHE_MANAGER})
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {
    public static final String PRIMARY_LOCAL_CACHE = PRIMARY + "LocalCache";
    public static final String PRIMARY_CACHE_SYNC = PRIMARY + "CacheSync";
    public static final String PRIMARY_REMOTE_CACHE = PRIMARY + "RemoteCache";
    public static final String PRIMARY_MULTILEVEL_CACHE = PRIMARY + "MultilevelCache";
    public static final String LOCAL_CACHE_MANAGER = PREFIX_CACHE + DOT + SCHEMA_CACHE_LOCAL + BEAN_CACHEMANAGER;
    public static final String PRIMARY_REMOTE_CACHE_MANAGER = PRIMARY + "RemoteCacheManager";
    @Bean(PRIMARY_LOCAL_CACHE)
    @OnMissingBean(name=PRIMARY_LOCAL_CACHE)
    @OnBean(name = {LOCAL_CACHE_MANAGER,PRIMARY_CACHE_SYNC})
    public LocalCache primaryLocalCache(@Qualifier(PRIMARY_CACHE_SYNC) CacheSync cs,@Qualifier(LOCAL_CACHE_MANAGER) CacheManager cm){
        Collection<String> cacheNames = cm.getCacheNames();
        return new LocalCache(cs,cacheNames.stream().findFirst().get());
    }

    @Bean(PRIMARY_REMOTE_CACHE)
    @OnMissingBean(name=PRIMARY_REMOTE_CACHE)
    @OnBean(name = PRIMARY_REMOTE_CACHE_MANAGER)
    public RemoteCache primaryRemoteCache(@Qualifier(PRIMARY_REMOTE_CACHE_MANAGER) CacheManager cm){
        Collection<String> cacheNames = cm.getCacheNames();
        String first = cacheNames.stream().findFirst().get();
        return new RemoteCache(CollectionUtils.newArrayList(cm.getCache(first)));
    }
    @Bean(PRIMARY_MULTILEVEL_CACHE)
    @OnMissingBean(name=PRIMARY_MULTILEVEL_CACHE)
    @OnBean(name = {PRIMARY_LOCAL_CACHE,PRIMARY_REMOTE_CACHE})
    public MultilevelCache primaryMultilevelCache(@Qualifier(PRIMARY_LOCAL_CACHE) LocalCache localCache,@Qualifier(PRIMARY_REMOTE_CACHE) RemoteCache remoteCache){
        MultilevelCache multilevelCache =  new MultilevelCache(localCache,remoteCache);
        return multilevelCache;
    }

}
