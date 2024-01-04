package org.bf.framework.autoconfigure.storage;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.support.storage.StorageProperties;
import org.bf.framework.boot.support.storage.StorageProxy;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration
@ConditionalOnProperty(prefix = StorageAutoConfig.PREFIX, name = ENABLED)
@ConditionalOnMissingBean(value = StorageAutoConfig.class)
@EnableConfig(StorageAutoConfig.class)
@Slf4j
public class StorageAutoConfig implements EnableConfigHandler<StorageProperties> {

    public static final String PREFIX = PREFIX_STORAGE;

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public StorageProperties bindInstance(Map<String, Object> properties) {
        return new StorageProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(properties);
        StorageProperties cfg = (StorageProperties)YamlUtil.getConfigBind(properties);
        if(cfg == null) {
            throw new RuntimeException("config empty");
        }
        StorageProxy proxy = null;
        if(CLOUD_PLATFORM_ALIYUN.equals(cfg.getPlatform())){
            proxy = new OssProxy(cfg);
        } else if(CLOUD_PLATFORM_AWS.equals(cfg.getPlatform())){
            proxy = new S3Proxy(cfg);
        } else if(CLOUD_PLATFORM_TENCENT.equals(cfg.getPlatform())){
            proxy = new CosProxy(cfg);
        } else {
            throw new RuntimeException("unknown cloud platform " + cfg.getPlatform());
        }
        Middleware middleware = new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(StorageProxy.class).setBean(proxy);
        return CollectionUtils.newArrayList(middleware);
    }
}
