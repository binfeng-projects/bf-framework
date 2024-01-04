package org.bf.framework.autoconfigure.xxljob;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@ConditionalOnMissingBean(value = XxlJobAutoConfig.class)
@EnableConfigurationProperties(XxlJobProperties.class)
@Slf4j
public class XxlJobAutoConfig {

    @Autowired
    XxlJobProperties cfg;

    @Bean
    @ConditionalOnMissingBean(XxlJobSpringExecutor.class)
    @ConditionalOnProperty(name = "bf.xxl.enabled", havingValue = "true")
    public XxlJobSpringExecutor xxlJobSpringExecutor() {
        if (cfg.getExecutor() == null) {
            throw new RuntimeException("execute cannot be empty");
        }
        if (cfg.getAdmin() == null) {
            throw new RuntimeException("admin cannot be empty");
        }
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(cfg.getAdmin().getAddresses());
        xxlJobSpringExecutor.setAppname(cfg.getExecutor().getAppname());
//        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(cfg.getExecutor().getPort());
        xxlJobSpringExecutor.setAccessToken(cfg.getAccessToken());
        xxlJobSpringExecutor.setLogPath(cfg.getExecutor().getLogpath());
        xxlJobSpringExecutor.setLogRetentionDays(cfg.getExecutor().getLogretentiondays());
        //自己注册
        return xxlJobSpringExecutor;
    }
}
