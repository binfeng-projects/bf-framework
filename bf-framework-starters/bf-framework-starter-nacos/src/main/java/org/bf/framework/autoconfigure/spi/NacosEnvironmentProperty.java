package org.bf.framework.autoconfigure.spi;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.spi.EnvironmentPropertyPostProcessor;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.core.Ordered;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import static org.bf.framework.boot.constant.FrameworkConst.DEFAULT;
import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.ENABLED;
import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_NACOS;

@Slf4j
public class NacosEnvironmentProperty implements EnvironmentPropertyPostProcessor {
    private static final String PREFIX = PREFIX_NACOS;
    private static final String MIDDLE_WARE = "middleware";

    /**
     * 先加载default命名空间的中间件配置，再加载 ${spring.profiles.active} 命名空间的中间件配置
     * 再加载default命名空间的用户配置, 再加载${spring.profiles.active} 命名空间的用户件配置
     * 优先级 用户的最高。 ${spring.profiles.active} 命名空间的用户件配置 > default命名空间的用户配置 > ${spring.profiles.active} 命名空间的中间件配置 > default命名空间的中间件配置
     */
    @Override
    public Map<String, Object> simpleProperties() {
        Map<String, Object> result = MapUtils.newHashMap();
        String serverAddr = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + PropertyKeyConst.SERVER_ADDR);
        if(StringUtils.isBlank(serverAddr)){
            return result;
        }
        String enabled = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + ENABLED);
        if(StringUtils.isBlank(enabled) || "false".equals(enabled)){
            return result;
        }
        String middlewareGroup = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + "middleware-group");
        if(StringUtils.isBlank(middlewareGroup)) {
            middlewareGroup = MIDDLE_WARE;
        }
        String middlewareDataIds = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + "middleware-dataIds");
        if(StringUtils.isBlank(middlewareDataIds)) {
            middlewareGroup = "common";
        }
        String userName = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + PropertyKeyConst.USERNAME);
        String password = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + PropertyKeyConst.PASSWORD);
        String accessKey = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + PropertyKeyConst.ACCESS_KEY);
        String secretKey = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + PropertyKeyConst.SECRET_KEY);
        try {
            //默认namespace,方便继承
            Properties defaultNs = new Properties();
            defaultNs.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
                defaultNs.put(PropertyKeyConst.USERNAME,userName);
                defaultNs.put(PropertyKeyConst.PASSWORD,password);
            } else if(StringUtils.isNotBlank(accessKey) && StringUtils.isNotBlank(secretKey)) {
                defaultNs.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                defaultNs.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            //和spring env想匹配的namespace,继承并覆盖default namespace
            Properties envNsProps = new Properties();
            envNsProps.putAll(defaultNs);
            defaultNs.put(PropertyKeyConst.NAMESPACE, DEFAULT);
            String namespace = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + PropertyKeyConst.NAMESPACE);
            envNsProps.put(PropertyKeyConst.NAMESPACE, namespace);

            ConfigService defaultSvc = NacosFactory.createConfigService(defaultNs);
            ConfigService envSvc = NacosFactory.createConfigService(envNsProps);
            //加载中间件默认的配置(一般是公共配置)
            result.putAll(loadNacosConfig(defaultSvc,middlewareGroup,middlewareDataIds));
            result.putAll(loadNacosConfig(envSvc,middlewareGroup,middlewareDataIds));
            String appGroup = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + "application-group");
            if(StringUtils.isBlank(appGroup)) {
                appGroup = SpringUtil.appName();
            }
            String appDataIds = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + "application-dataIds");
            if(StringUtils.isNotBlank(appDataIds)) {
                //加载业务定义的公共配置,可以覆盖默认中间件定义的，也就是业务的优先级更高
                result.putAll(loadNacosConfig(defaultSvc,appGroup,appDataIds));
                result.putAll(loadNacosConfig(envSvc,appGroup,appDataIds));
            }
        } catch (NacosException e) {
            log.error("error create Nacos config");
        }
        return result;
    }

    private Map<String,Object> loadNacosConfig(ConfigService configService,String groups,String dataIds){
        Map<String, Object> result = MapUtils.newHashMap();
        if(StringUtils.isBlank(groups) || StringUtils.isBlank(dataIds)) {
            return result;
        }
        String[] groupArray = groups.split(",");
        if(groupArray.length == 0) {
            return result;
        }
        String[] dataIdArray = dataIds.split(",");
        if(dataIdArray.length == 0) {
            return result;
        }
        for (String group : groupArray) {
            for (String dataId : dataIdArray) {
                try {
                    String content = configService.getConfig(dataId, group, 5000);
                    if(StringUtils.isBlank(content)) {
                        continue;
                    }
                    // 解析配置
                    // 假设配置内容是 key=value 格式，可以使用 Java 的 Properties 类来解析
                    Properties propertiesFromNacos = new Properties();
                    propertiesFromNacos.load(new StringReader(content));
                    for (Map.Entry<Object,Object> entry : propertiesFromNacos.entrySet()) {
                        result.put(String.valueOf(entry.getKey()),entry.getValue());
                    }
                } catch (Exception e) {
                    log.error("nacos error load property");
                }
            }
        }
        return result;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE -1;
    }

}
