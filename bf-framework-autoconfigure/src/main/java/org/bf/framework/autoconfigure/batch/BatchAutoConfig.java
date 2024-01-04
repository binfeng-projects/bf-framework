package org.bf.framework.autoconfigure.batch;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.jdbc.JdbcAutoConfig;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.configuration.support.ScopeConfiguration;
import org.springframework.batch.core.converter.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration(after = { JdbcAutoConfig.class})
@ConditionalOnMissingBean(value = BatchAutoConfig.class)
@EnableConfig(BatchAutoConfig.class)
@ConditionalOnClass({ JobLauncher.class, DataSource.class})
@Import(ScopeConfiguration.class)
@Slf4j
public class BatchAutoConfig implements EnableConfigHandler<BatchProperties> {

    public static final String PREFIX = PREFIX_BATCH;

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public BatchProperties bindInstance(Map<String, Object> properties) {
        return new BatchProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        //参考 SpringHadoopConfiguration
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            BatchProperties cfg = (BatchProperties) YamlUtil.getConfigBind(map);
            if (cfg == null || StringUtils.isBlank(cfg.getDataSourceRef())) {
                return null;
            }
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(BatchProxy.class).setBean(createConfig(cfg)));
        } catch (Exception e) {
            log.error("processRegisterBean error", e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public static BatchProxy createConfig(BatchProperties cfg){
        String dsBeanPrefix = YamlUtil.prefixToBeanName(PREFIX_DATASOURCE + DOT + cfg.getDataSourceRef());
        DataSource ds = SpringUtil.getBean(dsBeanPrefix + BEAN_DATASOURCE, DataSource.class);
        PlatformTransactionManager tm = null;
        if (ds == null) {
            ds = YamlUtil.configBind(PREFIX_DATASOURCE, cfg.getDataSourceRef(), JdbcAutoConfig::createProperty, null);
            if (ds == null) {
                return null;
            }
            tm = new DataSourceTransactionManager(ds);
        } else {
            //已经实例化，说明是使用了enable的datasource
            tm = SpringUtil.getBean(dsBeanPrefix + BEAN_PLATFORMTRANSACTIONMANAGER, PlatformTransactionManager.class);
        }
        BatchProxy proxy = new BatchProxy();
        proxy.setDataSource(ds);
        proxy.setTransactionManager(tm);
        proxy.setJobRepository(getJobRepository(ds,tm,cfg));
        proxy.setJobLauncher(getJobLauncher(proxy.getJobRepository()));
        proxy.setJobRegistry(new MapJobRegistry());
        proxy.setJobExplorer(getJobExplorer(ds, tm, cfg));
        JobOperatorFactoryBean factoryBean = new JobOperatorFactoryBean();
        factoryBean.setTransactionManager(tm);
        factoryBean.setJobExplorer(proxy.getJobExplorer());
        factoryBean.setJobRegistry(proxy.getJobRegistry());
        factoryBean.setJobRepository(proxy.getJobRepository());
        factoryBean.setJobLauncher(proxy.getJobLauncher());
        try {
            factoryBean.afterPropertiesSet();
            JobOperator jobOperator = factoryBean.getObject();
            proxy.setJobOperator(jobOperator);
        }
        catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the default job operator", e);
        }
        return proxy;
    }
    public static JobLauncher getJobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository);
        taskExecutorJobLauncher.setTaskExecutor(new SyncTaskExecutor());
        try {
            taskExecutorJobLauncher.afterPropertiesSet();
            return taskExecutorJobLauncher;
        }
        catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the default job launcher", e);
        }
    }
    public static JobExplorer getJobExplorer(DataSource ds,PlatformTransactionManager tm,BatchProperties cfg) {
        JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
        factoryBean.setDataSource(ds);
        factoryBean.setTransactionManager(tm);
        factoryBean.setJdbcOperations(new JdbcTemplate(ds));
        factoryBean.setCharset(StandardCharsets.UTF_8);
        factoryBean.setTablePrefix(cfg.getTablePrefix());
        factoryBean.setLobHandler(new DefaultLobHandler());
        factoryBean.setConversionService(getConversionService());
        factoryBean.setSerializer(new DefaultExecutionContextSerializer());
        try {
            factoryBean.afterPropertiesSet();
            return factoryBean.getObject();
        }
        catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the default job explorer", e);
        }
    }

    private static JobRepository getJobRepository(DataSource ds,PlatformTransactionManager tm,BatchProperties cfg) {
        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        try {
            factoryBean.setDataSource(ds);
            factoryBean.setTransactionManager(tm);
            factoryBean.setJdbcOperations(new JdbcTemplate(ds));
            factoryBean.setCharset(StandardCharsets.UTF_8);
            factoryBean.setTablePrefix(cfg.getTablePrefix());
            factoryBean.setLobHandler(new DefaultLobHandler());
            factoryBean.setConversionService(getConversionService());
            factoryBean.setSerializer(new DefaultExecutionContextSerializer());
//            factoryBean.setDatabaseType(getDatabaseType());
            factoryBean.setIncrementerFactory(new DefaultDataFieldMaxValueIncrementerFactory(ds));
            factoryBean.setClobType(Types.CLOB);
            factoryBean.setMaxVarCharLength(AbstractJdbcBatchMetadataDao.DEFAULT_EXIT_MESSAGE_LENGTH);
            factoryBean.setIsolationLevelForCreateEnum(cfg.getIsolationLevelForCreate());
            factoryBean.setValidateTransactionState(true);
            factoryBean.afterPropertiesSet();
            return factoryBean.getObject();
        } catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the default job repository", e);
        }
    }
    private static ConfigurableConversionService getConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new DateToStringConverter());
        conversionService.addConverter(new StringToDateConverter());
        conversionService.addConverter(new LocalDateToStringConverter());
        conversionService.addConverter(new StringToLocalDateConverter());
        conversionService.addConverter(new LocalTimeToStringConverter());
        conversionService.addConverter(new StringToLocalTimeConverter());
        conversionService.addConverter(new LocalDateTimeToStringConverter());
        conversionService.addConverter(new StringToLocalDateTimeConverter());
        return conversionService;
    }
}
