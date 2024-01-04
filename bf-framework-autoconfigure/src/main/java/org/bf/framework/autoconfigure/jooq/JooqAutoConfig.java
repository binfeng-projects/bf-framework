package org.bf.framework.autoconfigure.jooq;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.jdbc.JdbcAutoConfig;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;

import org.bf.framework.boot.util.SpringUtil;
import org.jooq.SQLDialect;
import org.jooq.TransactionProvider;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.tools.jdbc.JDBCUtils;
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator;
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration(before = {JooqAutoConfiguration.class},after = {JdbcAutoConfig.class})
@ConditionalOnClass(DSLContext.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnMissingBean(JooqAutoConfig.class)
@EnableConfig(JooqAutoConfig.class)
@Slf4j
public class JooqAutoConfig implements EnableConfigHandler<DataSource> {

    private static final String PREFIX = PREFIX_DATASOURCE;

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
            String prefixAndSchema = PREFIX + DOT + schema;
            Configuration cfg = genConfig(prefixAndSchema);
            if(cfg == null){
                return result;
            }
            DSLContext dslContext = new DefaultDSLContext(cfg);
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(DSLContext.class).setBean(dslContext));
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
    public Configuration genConfig(String prefixAndSchema){
        //bean的名称是有一定规范的，外界可按命名规范注入bean,我们也可以根据规范得到bean
        DataSource ds = SpringUtil.getBean(prefixAndSchema + BEAN_DATASOURCE,DataSource.class);
        PlatformTransactionManager tm = SpringUtil.getBean(prefixAndSchema + BEAN_PLATFORMTRANSACTIONMANAGER,PlatformTransactionManager.class);;
        if (ds == null || tm == null) {
            return null;
        }
        Configuration cfg = new DefaultConfiguration();
        DataSourceConnectionProvider cp = new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(ds));
        cfg.set(cp);
        TransactionProvider tp = new SpringTransactionProvider(tm);
        cfg.set(tp);
        cfg.set(new DefaultExecuteListenerProvider(new JooqExceptionTranslator()));
        cfg.set(getDialect(ds));
        return cfg;
    }
    static SQLDialect getDialect(DataSource dataSource) {
        if (dataSource == null) {
            return SQLDialect.DEFAULT;
        }
        try {
            String url = JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getURL);
            return JDBCUtils.dialect(url);
        }
        catch (MetaDataAccessException ex) {
            log.warn("Unable to determine jdbc url from datasource", ex);
        }
        return SQLDialect.DEFAULT;
    }
}
