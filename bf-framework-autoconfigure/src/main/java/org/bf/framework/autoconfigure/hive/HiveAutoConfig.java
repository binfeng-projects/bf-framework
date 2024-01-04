package org.bf.framework.autoconfigure.hive;

import lombok.extern.slf4j.Slf4j;
import org.apache.hive.jdbc.HiveDriver;
import org.bf.framework.autoconfigure.batch.BatchAutoConfig;
import org.bf.framework.autoconfigure.hadoop.HadoopAutoConfig;
import org.bf.framework.autoconfigure.hive.support.HiveClient;
import org.bf.framework.autoconfigure.hive.support.HiveTemplate;
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
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration(after = { HadoopAutoConfig.class,BatchAutoConfig.class})
@ConditionalOnMissingBean(value = HiveAutoConfig.class)
@EnableConfig(HiveAutoConfig.class)
@ConditionalOnClass({HiveDriver.class})
@Slf4j
public class HiveAutoConfig implements EnableConfigHandler<HiveProperties>{

    public static final String PREFIX = PREFIX_HIVE;
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public HiveProperties bindInstance(Map<String, Object> properties) {
        return new HiveProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        //参考 SpringHadoopConfiguration
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            HiveProperties cfg = (HiveProperties) YamlUtil.getConfigBind(map);
            if (cfg == null || StringUtils.isBlank(cfg.getHiveUrl())) {
                return null;
            }
//            String hadoopBeanPrefix = YamlUtil.prefixToBeanName(PREFIX_HADOOP + DOT + cfg.getHadoopRef());
//            Configuration hadoopCfg = SpringUtil.getBean(hadoopBeanPrefix + BEAN_CONFIGURATION, Configuration.class);
//            if (hadoopCfg == null) {
//                HadoopProperties hadoopProperties = YamlUtil.configBind(PREFIX_DATASOURCE, cfg.getHadoopRef(), new HadoopProperties());
//                hadoopCfg = HadoopAutoConfig.createConfig(hadoopProperties);
//            }
            SimpleDriverDataSource ds = new SimpleDriverDataSource(new HiveDriver(), cfg.getHiveUrl());
            java.sql.Connection con = DataSourceUtils.getConnection(ds);
            SingleConnectionDataSource factoryDataSource = new SingleConnectionDataSource(con, true);
            HiveClient hiveClient = new HiveClient(factoryDataSource);
            HiveTemplate template = new HiveTemplate(hiveClient);
            template.setResourceLoader(SpringUtil.getContext());
			result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(HiveTemplate.class).setBean(template));

        } catch (Exception e) {
            log.error("processRegisterBean error", e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
}
