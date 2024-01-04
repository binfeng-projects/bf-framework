package org.bf.framework.autoconfigure.vms;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.support.vms.VmsProperties;
import org.bf.framework.boot.support.vms.VmsProxy;
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
@ConditionalOnProperty(prefix = VmsAutoConfig.PREFIX, name = ENABLED)
@ConditionalOnMissingBean(value = VmsAutoConfig.class)
@EnableConfig(VmsAutoConfig.class)
@Slf4j
public class VmsAutoConfig implements EnableConfigHandler<VmsProperties> {

    public static final String PREFIX = PREFIX_VMS;

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public VmsProperties bindInstance(Map<String, Object> properties) {
        return new VmsProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(properties);
        VmsProperties cfg = (VmsProperties)YamlUtil.getConfigBind(properties);
        if(cfg == null) {
            throw new RuntimeException("config empty");
        }
        VmsProxy proxy = null;
        if(CLOUD_PLATFORM_ALIYUN.equals(cfg.getPlatform())){
            proxy = new AliyunProxy(cfg);
        } else if(CLOUD_PLATFORM_AWS.equals(cfg.getPlatform())){
            proxy = new AwsProxy(cfg);
        } else if(CLOUD_PLATFORM_TENCENT.equals(cfg.getPlatform())){
            proxy = new TencentProxy(cfg);
        } else {
            throw new RuntimeException("unknown cloud platform " + cfg.getPlatform());
        }
        Middleware middleware = new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(VmsProxy.class).setBean(proxy);
        return CollectionUtils.newArrayList(middleware);
    }
}
