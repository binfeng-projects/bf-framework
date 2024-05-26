package org.bf.framework.autoconfigure.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.discovery.EnableNacosDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static org.bf.framework.boot.constant.MiddlewareConst.ENABLED;
import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_NACOS;

@AutoConfiguration
@ConditionalOnMissingBean(value = NacosAutoConfig.class)
@EnableConfigurationProperties(NacosCfgProperties.class)
@ConditionalOnClass(name = {"org.springframework.boot.context.properties.bind.Binder"})
@ConditionalOnProperty(prefix = PREFIX_NACOS, name = {PropertyKeyConst.SERVER_ADDR,PropertyKeyConst.NAMESPACE,ENABLED})
@Slf4j
public class NacosAutoConfig {
    public static final String USERNAME_PLACEHOLDER = "${nacos.username:${bf.nacos.username:}}";
    public static final String PASSWORD_PLACEHOLDER = "${nacos.password:${bf.nacos.password:}}";
    public static final String ENDPOINT_PLACEHOLDER = "${nacos.endpoint:${bf.nacos.endpoint:}}";
    public static final String ACCESS_KEY_PLACEHOLDER = "${nacos.access-key:${bf.nacos.access-key:}}";
    public static final String SECRET_KEY_PLACEHOLDER = "${nacos.secret-key:${bf.nacos.secret-key:}}";
    public static final String SERVER_ADDR_PLACEHOLDER = "${nacos.server-addr:${bf.nacos.server-addr:}}";
    public static final String CLUSTER_NAME_PLACEHOLDER = "${nacos.cluster-name:${bf.nacos.cluster-name:}}";
    public static final String ENCODE_PLACEHOLDER = "${nacos.encode:${bf.nacos.encode:UTF-8}}";
    public static final String CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER = "${nacos.configLongPollTimeout:${bf.nacos.configLongPollTimeout:}}";
    public static final String CONFIG_RETRY_TIME_PLACEHOLDER = "${nacos.configRetryTime:${bf.nacos.configRetryTime:}}";
    public static final String MAX_RETRY_PLACEHOLDER = "${nacos.maxRetry:${bf.nacos.maxRetry:}}";
    public static final String NAMESPACE_PLACEHOLDER = "${nacos.namespace:${bf.nacos.namespace:}}";
    public static final String CONTEXT_PATH_PLACEHOLDER = "${nacos.context-path:${bf.nacos.context-path:}}";
    public static final String DISCOVERY_CONTEXT_PATH_PLACEHOLDER = "${nacos.discovery.context-path:${bf.nacos.discoveryContextPath:}}";
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(name = {"globalNacosProperties$config"})
    @EnableNacosConfig(globalProperties = @NacosProperties(
    username = USERNAME_PLACEHOLDER,
    password = PASSWORD_PLACEHOLDER,
    endpoint = ENDPOINT_PLACEHOLDER,
    namespace = NAMESPACE_PLACEHOLDER,
    accessKey = ACCESS_KEY_PLACEHOLDER,
    secretKey = SECRET_KEY_PLACEHOLDER,
    serverAddr = SERVER_ADDR_PLACEHOLDER,
    contextPath = CONTEXT_PATH_PLACEHOLDER,
    clusterName = CLUSTER_NAME_PLACEHOLDER,
    encode = ENCODE_PLACEHOLDER,
    configLongPollTimeout = CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER,
    configRetryTime = CONFIG_RETRY_TIME_PLACEHOLDER,
    maxRetry = MAX_RETRY_PLACEHOLDER))
    public static class NacosConfigConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(name = {"globalNacosProperties$discovery"})
    @ConditionalOnProperty(prefix = PREFIX_NACOS, name = "discoveryContextPath")
    @EnableNacosDiscovery(globalProperties = @NacosProperties(
            username = USERNAME_PLACEHOLDER,
            password = PASSWORD_PLACEHOLDER,
            endpoint = ENDPOINT_PLACEHOLDER,
            namespace = NAMESPACE_PLACEHOLDER,
            accessKey = ACCESS_KEY_PLACEHOLDER,
            secretKey = SECRET_KEY_PLACEHOLDER,
            serverAddr = SERVER_ADDR_PLACEHOLDER,
            contextPath = DISCOVERY_CONTEXT_PATH_PLACEHOLDER,
            clusterName = CLUSTER_NAME_PLACEHOLDER,
            encode = ENCODE_PLACEHOLDER))
    public static class NacosDiscoveryConfiguration {
    }
}
