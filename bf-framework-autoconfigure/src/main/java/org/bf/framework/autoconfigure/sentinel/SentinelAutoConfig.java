package org.bf.framework.autoconfigure.sentinel;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration
@ConditionalOnClass({SentinelResource.class})
@ConditionalOnMissingBean(value = SentinelAutoConfig.class)
@EnableConfigurationProperties(SentinelProperties.class)
@ConditionalOnProperty(prefix = PREFIX_SENTINEL, name = "dashboard")
@Slf4j
public class SentinelAutoConfig {
    @Autowired
    SentinelProperties cfg;
    @Bean
    @ConditionalOnMissingBean(SentinelProxy.class)
    public SentinelProxy sentinelProxy() {
        SentinelProxy proxy = null;
        if("apollo".equals(cfg.getDataSourceType())){
            proxy = new ApolloSentinelProxy(cfg);
        } else if("redis".equals(cfg.getDataSourceType())){
            proxy = new RedisSentinelProxy(cfg);
        } else if("zookeeper".equals(cfg.getDataSourceType())){
            proxy = new ZookeeperSentinelProxy(cfg);
        } else if("nacos".equals(cfg.getDataSourceType())){
            proxy = new NacosSentinelProxy(cfg);
        } else {
            throw new RuntimeException("unknown sentinel dataSourceType " + cfg.getDataSourceType());
        }
        proxy.loadRules();
        return proxy;
    }
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
