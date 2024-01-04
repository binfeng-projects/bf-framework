package org.bf.framework.autoconfigure.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.cache.CacheConfig;
import org.bf.framework.boot.support.cache.LocalCacheProperties;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration
@ConditionalOnClass({Caffeine.class})
@ConditionalOnMissingBean(value = CaffeineAutoConfig.class,name = CacheConfig.LOCAL_CACHE_MANAGER)
@EnableConfig(CaffeineAutoConfig.class)
@Slf4j
public class CaffeineAutoConfig implements EnableConfigHandler<LocalCacheProperties> {
    private static final String PREFIX = PREFIX_CACHE;
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public LocalCacheProperties bindInstance(Map<String, Object> properties) {
        return LocalCacheProperties.newConfig(properties);
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(properties);
        try {
            List<Middleware> result = CollectionUtils.newArrayList();
            //不注册bean，只注册key
            LocalCacheProperties cfg = (LocalCacheProperties)YamlUtil.getConfigBind(properties);
            Caffeine<Object, Object> caffeine = null;
            if(StringUtils.isNotBlank(cfg.getSpec())) {
                caffeine = Caffeine.from(cfg.getSpec());
            } else {
                caffeine = Caffeine.newBuilder();
            }
            if(cfg.getExpireAfterWrite() > 0) {
                caffeine.expireAfterWrite(Duration.ofSeconds(cfg.getExpireAfterWrite()));
            }
            if(cfg.getInitialCapacity() > 0) {
                caffeine.initialCapacity(cfg.getInitialCapacity());
            }
            if(cfg.getMaximumSize() > 0) {
                caffeine.maximumSize(cfg.getMaximumSize());
            }
            result.add(new Middleware().setCodeGen(false).setPrefix(PREFIX).setSchemaName(schema).setBean(new CaffeineCache(schema, caffeine.build())));
            String prefixAndSchema = PREFIX + DOT + SCHEMA_CACHE_LOCAL;
            Middleware.register(prefixAndSchema,result);
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        //这里处理不一样，自己注册，不让系统默认注册
        return null;
    }
    @Bean(CacheConfig.LOCAL_CACHE_MANAGER)
    @Primary
    @ConditionalOnMissingBean(name = CacheConfig.LOCAL_CACHE_MANAGER)
    public CacheManager cacheManager() {
        //初始化本地缓存
        List<Cache> ccs = CollectionUtils.newArrayList();
        List<Middleware> middlewareList = Middleware.getByPrefixAndSchema(PREFIX,SCHEMA_CACHE_LOCAL);
        for (Middleware m : middlewareList) {
            ccs.add((Cache) m.getBean());
        }
        SimpleCacheManager cm = new SimpleCacheManager();
        cm.setCaches(ccs);
        String prefixAndSchema = PREFIX + DOT + SCHEMA_CACHE_LOCAL;
        Middleware.register(prefixAndSchema,CollectionUtils.newArrayList(new Middleware().setPrefix(PREFIX).setSchemaName(SCHEMA_CACHE_LOCAL).setType(CacheManager.class).setBean(cm).setCodeGen(null)));
        return cm;
    }
}
