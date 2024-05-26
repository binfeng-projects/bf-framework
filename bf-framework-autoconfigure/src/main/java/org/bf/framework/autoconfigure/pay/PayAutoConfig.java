package org.bf.framework.autoconfigure.pay;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
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
@ConditionalOnProperty(prefix = PayAutoConfig.PREFIX, name = ENABLED)
@ConditionalOnMissingBean(value = PayAutoConfig.class)
@EnableConfig(PayAutoConfig.class)
@Slf4j
public class PayAutoConfig implements EnableConfigHandler<PayProperties> {
    public static final String PREFIX = PREFIX_PAY;
    @Override
    public String getPrefix() {
        return PREFIX;
    }
    @Override
    public PayProperties bindInstance(Map<String, Object> properties) {
        return new PayProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(properties);
        PayProperties cfg = (PayProperties)YamlUtil.getConfigBind(properties);
        if(cfg == null) {
            throw new RuntimeException("config empty");
        }
        PayProxy proxy = null;
        if(PAY_PLATFORM_ALI.equals(cfg.getPayPlatform())){
            proxy = new AliPayProxy(cfg);
        } else if(PAY_PLATFORM_WECHAT.equals(cfg.getPayPlatform())){
            proxy = new WechatPayProxy(cfg);
        } else if(PAY_PLATFORM_PAYPAL.equals(cfg.getPayPlatform())){
            proxy = new PaypalProxy(cfg);
        } else {
            throw new RuntimeException("unknown pay type " + cfg.getPayPlatform());
        }
        Middleware middleware = new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(PayProxy.class).setBean(proxy);
        return CollectionUtils.newArrayList(middleware);
    }
}
