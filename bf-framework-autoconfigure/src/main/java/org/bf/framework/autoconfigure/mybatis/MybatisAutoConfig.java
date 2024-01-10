package org.bf.framework.autoconfigure.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.jdbc.JdbcAutoConfig;
import org.bf.framework.autoconfigure.jooq.JooqAutoConfig;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration(after = {JdbcAutoConfig.class})
@ConditionalOnClass(MapperScan.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnMissingBean({MybatisAutoConfig.class, JooqAutoConfig.class})
@ConditionalOnProperty(prefix = PREFIX_MYBATIS, name = ENABLED)
@EnableConfig(MybatisAutoConfig.class)
@Slf4j
public class MybatisAutoConfig implements EnableConfigHandler<DataSource> {

    private static final String PREFIX = PREFIX_DATASOURCE;
    public static final String MAPPER_XML_PATH = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +"/mybatis/*/%s/*.xml";
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public DataSource bindInstance(Map<String,Object> map) {
        return null;
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            String schema = YamlUtil.getConfigSchema(properties);
            //bean的名称是有一定规范的，外界可按命名规范注入bean
            SqlSessionFactory sqlSessionFactory = genConfig(schema);
            if(sqlSessionFactory == null){
                return result;
            }
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(SqlSessionFactory.class).setBean(sqlSessionFactory));
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
    public SqlSessionFactory genConfig(String schema){
        String prefixAndSchema = PREFIX + DOT + schema;
        //bean的名称是有一定规范的，外界可按命名规范注入bean,我们也可以根据规范得到bean
        DataSource ds = SpringUtil.getBean(prefixAndSchema + BEAN_DATASOURCE,DataSource.class);
        PlatformTransactionManager tm = SpringUtil.getBean(prefixAndSchema + BEAN_PLATFORMTRANSACTIONMANAGER,PlatformTransactionManager.class);;
        if (ds == null || tm == null) {
            return null;
        }
        try {
            log.info("------------- sqlSessionFactory init-------");
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(ds);
            factory.setMapperLocations(SpringUtil.RESOURCE_RESOLVER.getResources(String.format(MAPPER_XML_PATH,schema)));
//            factory.setPlugins(new Interceptor[] {interceptor});
//            factory.setTypeEnumsPackage("com.jason.mytest.example.core.enums");
            return factory.getObject();
        } catch (Exception e) {
            log.error("mybatis config error",e);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(String.format(MAPPER_XML_PATH,"bf_security"));
    }
}
