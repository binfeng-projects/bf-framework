package org.bf.framework.autoconfigure.jdbc;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass({DataSource.class})
@ConditionalOnMissingBean(JdbcAutoConfig.class)
@EnableConfig(JdbcAutoConfig.class)
@Slf4j
public class JdbcAutoConfig implements EnableConfigHandler<DataSource> {
    private static final String PREFIX = PREFIX_DATASOURCE;
    private static final String DATASOURCE_TYPE_KEY = TYPE;
    private static final String DRIVER_TYPE_KEY = "driver-class-name";
    private static final String DRIVER_TYPE_DEFAULT = "com.mysql.cj.jdbc.Driver";
    private static final String DATASOURCE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public DataSource bindInstance(Map<String, Object> properties) {
        return createProperty(properties);
    }
    public static DataSource createProperty(Map<String,Object> map) {
        //默认数据源
        Class<? extends DataSource> clsType = HikariDataSource.class;
        String type = (String)map.get(DATASOURCE_TYPE_KEY);
        if(StringUtils.isNotBlank(type) && !type.equals(DATASOURCE_DEFAULT)) {
            try {
                clsType =(Class<? extends DataSource>) Class.forName(type);
            } catch (Exception e){
            }
        }
        DataSourceBuilder builder = DataSourceBuilder.create().type(clsType);
        //如果驱动不指定，用默认驱动com.mysql.cj.jdbc.Driver
        String driverClass = (String)map.get(DRIVER_TYPE_KEY);
        if(StringUtils.isBlank(driverClass)) {
            driverClass = DRIVER_TYPE_DEFAULT;
        }
        builder.driverClassName(driverClass);
        builder.url((String)map.get(URL));
        //实例化数据源
        return builder.build();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(properties);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            DataSource dataSource = (DataSource)(YamlUtil.getConfigBind(properties));
            PlatformTransactionManager dstm = new DataSourceTransactionManager(dataSource);
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(DataSource.class).setBean(dataSource));
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(PlatformTransactionManager.class).setBean(dstm));
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

//    @Configuration(proxyBeanMethods = false)
//    @EnableTransactionManagement
//    static class EnableConfiguration {
//
//    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = false)
    @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false")
    public static class JdkDynamicAutoProxyConfiguration {

    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true", matchIfMissing = true)
    public static class CglibAutoProxyConfiguration {

    }

}
