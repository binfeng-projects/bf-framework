package org.bf.framework.autoconfigure.actuator;

import cn.hutool.core.net.NetUtil;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.util.JSON;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.bf.framework.common.util.http.HttpUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.*;

@AutoConfiguration
@ConditionalOnClass(MeterFilter.class)
@ConditionalOnMissingBean(value = ActuatorAutoConfig.class)
@Slf4j
public class ActuatorAutoConfig {
    public static final String LOCAL_IP = NetUtil.getLocalhostStr();
    public static final String CONSUL_ADDR_KEY = "bf.consul.host";
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.commonTags(Arrays.asList(
                //配置文件默认就写入了
//                Tag.of("application", appName),
//                Tag.of("service","spring-boot"),
                Tag.of("ip", LOCAL_IP),
                Tag.of("cluster", SpringUtil.currentCluster()),
                Tag.of("env",SpringUtil.currentEnv()),
                Tag.of("frameworkVersion",SpringUtil.frameworkVersion())
        ));
    }

    @EventListener(value = {ApplicationReadyEvent.class})
    public void registerPrometheusWithConsul(ApplicationReadyEvent event){
        try{
            ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
            //TODO fixme
//            if(SpringUtil.notProd()){ //生产环境才注册
//                return;
//            }
            int port = Integer.parseInt(environment.getProperty("management.server.port"));
            // register app to consul
            Map<String,Object> data = new HashMap<>();
            String id = LOCAL_IP + ":" + port;
            data.put("id",id);
            data.put("name",SpringUtil.appName());
            data.put("address",LOCAL_IP);
            data.put("port",port);
            data.put("enableTagOverride",false);
            List<String> tags = new ArrayList<String>();
            tags.add("metrics=true"); //consul配置过滤该标签的注册服务到Prometheus
            data.put("tags",tags);
            Map<String,Object> checkHealth = MapUtils.newHashMap();
            checkHealth.put("http","http://" + id + "/metrics");
            checkHealth.put("interval","10s");
            data.put("check",checkHealth);
            Map<String,Object> meta = MapUtils.newHashMap(); //最终会转换成Prometheus的label
            meta.put("cluster", SpringUtil.currentCluster());
            meta.put("env", SpringUtil.currentEnv());
            meta.put("frameworkVersion", SpringUtil.frameworkVersion());
            meta.put("service", "spring-boot");
            data.put("meta",meta);

            String consulHost = environment.getProperty(CONSUL_ADDR_KEY);
            if(StringUtils.isBlank(consulHost)) {
                return;
            }
            String registerApi = consulHost +"/v1/agent/service/register?replace-existing-checks=1";
            HttpUtil.putJSON(registerApi, JSON.toJSONString(data),null);
            log.info("register to consul {} ",registerApi);

            // deRegister app from consul when app shutdown
            Runtime.getRuntime().addShutdownHook(new Thread( () -> {
                String deregisterApi = consulHost + "/v1/agent/service/deregister/" + id;
                HttpUtil.putJSON(deregisterApi,"",null);
                log.info("deregister from consul {} ",deregisterApi);
            }));
        }catch (Throwable t){
            log.error("registerPrometheusWithConsul failed.",t);
        }
    }

}
