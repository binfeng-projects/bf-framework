package org.bf.framework.autoconfigure;

import org.bf.framework.autoconfigure.caffeine.CaffeineAutoConfig;
import org.bf.framework.autoconfigure.redis.RedisAutoConfig;
import org.bf.framework.boot.config.FrameworkConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

//    @Order(Ordered.LOWEST_PRECEDENCE - 1)
@AutoConfiguration(after = {CaffeineAutoConfig.class, RedisAutoConfig.class})
@ConditionalOnMissingBean(FrameworkAutoConfig.class)
public class FrameworkAutoConfig {
    @Configuration(proxyBeanMethods = false)
    @Import(FrameworkConfiguration.class)
    protected static class FrameworkConfig {

    }

}
