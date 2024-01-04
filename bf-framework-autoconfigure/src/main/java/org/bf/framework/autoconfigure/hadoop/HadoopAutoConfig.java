package org.bf.framework.autoconfigure.hadoop;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.bf.framework.autoconfigure.batch.BatchAutoConfig;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.hadoop.config.annotation.EnableHadoop;
import org.springframework.data.hadoop.config.annotation.builders.SpringHadoopConfigBuilder;
import org.springframework.data.hadoop.config.common.annotation.ObjectPostProcessor;

import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration(after = { BatchAutoConfig.class})
@ConditionalOnMissingBean(value = HadoopAutoConfig.class)
@EnableConfig(HadoopAutoConfig.class)
@ConditionalOnClass({EnableHadoop.class, Configuration.class})
@Slf4j
public class HadoopAutoConfig implements EnableConfigHandler<HadoopProperties> {

    public static final String PREFIX = PREFIX_HADOOP;

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public HadoopProperties bindInstance(Map<String, Object> properties) {
        return new HadoopProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        //参考 SpringHadoopConfiguration
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        HadoopProperties cfg = (HadoopProperties) YamlUtil.getConfigBind(map);
        Configuration hadoopCfg = createConfig(cfg);
        if(hadoopCfg != null) {
            HadoopProxy proxy = new HadoopProxy();
            proxy.setConfiguration(hadoopCfg);
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(HadoopProxy.class).setBean(proxy));
//			FsShell fsShell = new FsShell(hadoopConfiguration);
//			result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(FsShell.class).setBean(fsShell));
        }
        return result;
    }

    public static Configuration createConfig(HadoopProperties cfg){
        try {
            if(cfg == null || StringUtils.isBlank(cfg.getFsUri())) {
                return null;
            }
            cfg.setEnvironment(SpringUtil.getEnvironment());
            SpringHadoopConfig shc = new SpringHadoopConfig(cfg);
            shc.setObjectPostProcessor(new CommonObjectProcessor());
            SpringHadoopConfigBuilder builder = new SpringHadoopConfigBuilder();
            builder.apply(shc);
            return builder.getOrBuild().getConfiguration();
        } catch (Exception e) {
            log.error("processRegisterBean error", e);
            return null;
        }
    }

//    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
//    @Import(ObjectPostProcessorConfiguration.class)
//    public class YarnAutoConfig {
//
//    }
}
