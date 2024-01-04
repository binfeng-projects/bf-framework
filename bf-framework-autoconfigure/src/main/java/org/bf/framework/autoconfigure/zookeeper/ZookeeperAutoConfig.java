package org.bf.framework.autoconfigure.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_ZOOKEEPER;

@AutoConfiguration
@ConditionalOnClass(CuratorFramework.class)
@ConditionalOnMissingBean(value = ZookeeperAutoConfig.class)
@EnableConfig(ZookeeperAutoConfig.class)
@Slf4j
public class ZookeeperAutoConfig implements EnableConfigHandler<ZookeeperProperties> {
    private static final String PREFIX = PREFIX_ZOOKEEPER;
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ZookeeperProperties bindInstance(Map<String, Object> properties) {
        return new ZookeeperProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        List<Middleware> result = CollectionUtils.newArrayList();
        String schema = YamlUtil.getConfigSchema(map);
        ZookeeperProperties cfg = (ZookeeperProperties) YamlUtil.getConfigBind(map);
        RetryPolicy retryPolicy  = new ExponentialBackoffRetry(cfg.getBaseSleepTimeMs(), cfg.getMaxRetries());
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(cfg.getUrl())
                .sessionTimeoutMs(cfg.getSessionTimeoutMs())
                .connectionTimeoutMs(cfg.getConnectionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(cfg.getNamespace())
                .build();
        client.start();
        result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(CuratorFramework.class).setBean(client));
        return result;
    }
}
