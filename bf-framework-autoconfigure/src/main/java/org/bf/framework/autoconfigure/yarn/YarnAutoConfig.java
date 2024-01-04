package org.bf.framework.autoconfigure.yarn;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.bf.framework.autoconfigure.batch.BatchAutoConfig;
import org.bf.framework.autoconfigure.batch.BatchProperties;
import org.bf.framework.autoconfigure.batch.BatchProxy;
import org.bf.framework.autoconfigure.hadoop.CommonObjectProcessor;
import org.bf.framework.autoconfigure.hadoop.HadoopAutoConfig;
import org.bf.framework.autoconfigure.hadoop.HadoopProperties;
import org.bf.framework.autoconfigure.yarn.properties.*;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.hadoop.util.net.DefaultHostInfoDiscovery;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.yarn.YarnSystemConstants;
import org.springframework.yarn.am.AbstractAppmaster;
import org.springframework.yarn.am.YarnAppmaster;
import org.springframework.yarn.am.cluster.AbstractContainerClusterAppmaster;
import org.springframework.yarn.am.cluster.ContainerClusterStateMachineConfiguration;
import org.springframework.yarn.am.grid.support.DefaultGridProjectionFactory;
import org.springframework.yarn.am.grid.support.GridProjectionFactoryRegistry;
import org.springframework.yarn.am.grid.support.ProjectionData;
import org.springframework.yarn.am.grid.support.ProjectionDataRegistry;
import org.springframework.yarn.batch.am.AbstractBatchAppmaster;
import org.springframework.yarn.batch.container.AbstractBatchYarnContainer;
import org.springframework.yarn.batch.container.DefaultBatchYarnContainer;
import org.springframework.yarn.batch.repository.RemoteJobExplorer;
import org.springframework.yarn.batch.support.BeanFactoryStepLocator;
import org.springframework.yarn.batch.support.YarnJobLauncher;
import org.springframework.yarn.boot.support.AppmasterLauncherRunner;
import org.springframework.yarn.boot.support.BootApplicationEventTransformer;
import org.springframework.yarn.boot.support.ContainerLauncherRunner;
import org.springframework.yarn.boot.support.EmbeddedAppmasterTrackService;
import org.springframework.yarn.client.YarnClient;
import org.springframework.yarn.config.annotation.SpringYarnAnnotationPostProcessor;
import org.springframework.yarn.config.annotation.SpringYarnConfigs;
import org.springframework.yarn.config.annotation.builders.SpringYarnConfigBuilder;
import org.springframework.yarn.container.AbstractYarnContainer;
import org.springframework.yarn.container.YarnContainer;
import org.springframework.yarn.event.DefaultYarnEventPublisher;
import org.springframework.yarn.event.YarnEventPublisher;
import org.springframework.yarn.fs.ResourceLocalizer;
import org.springframework.yarn.integration.ip.mind.DefaultMindAppmasterServiceClient;
import org.springframework.yarn.support.ParsingUtils;
import org.springframework.yarn.support.YarnContextUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

/**
 * exclude掉四个auto。总共6个，剩余两个RestTemplate保留
 *         - org.springframework.yarn.boot.YarnClientAutoConfiguration
 *         - org.springframework.yarn.boot.YarnContainerAutoConfiguration
 *         - org.springframework.yarn.boot.YarnAppmasterAutoConfiguration
 *         - org.springframework.yarn.boot.ContainerClusterAppmasterAutoConfiguration
 *
 *         property配置参考 https://docs.spring.io/spring-hadoop/docs/current/reference/html/springandhadoop-yarn.html
 */
@AutoConfiguration(after = { HadoopAutoConfig.class,BatchAutoConfig.class})
@ConditionalOnMissingBean(value = YarnAutoConfig.class)
@EnableConfig(YarnAutoConfig.class)
@ConditionalOnClass({YarnConfiguration.class})
@Import(ContainerClusterStateMachineConfiguration.class)
@Slf4j
public class YarnAutoConfig implements EnableConfigHandler<SpringYarnProperties>, ApplicationEventPublisherAware {

    public static final String PREFIX = PREFIX_YARN;
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public SpringYarnProperties bindInstance(Map<String, Object> properties) {
        return new SpringYarnProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        //参考 SpringHadoopConfiguration
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            SpringYarnProperties cfg = (SpringYarnProperties) YamlUtil.getConfigBind(map);
            if (cfg == null || StringUtils.isBlank(cfg.getHadoopRef())) {
                return null;
            }
            HadoopProperties hadoopProperties = YamlUtil.configBind(PREFIX_HADOOP, cfg.getHadoopRef(), new HadoopProperties());
            if (hadoopProperties == null) {
                throw new RuntimeException("hadoop config null");
            }
            cfg.setHadoopProperties(hadoopProperties);
            SpringYarnConfigBuilder builder = new SpringYarnConfigBuilder();
            SpringYarnConfig springYarnConfig = new SpringYarnConfig(cfg);
            springYarnConfig.setObjectPostProcessor(new CommonObjectProcessor());
            builder.apply(springYarnConfig);
            SpringYarnConfigs cfgs = builder.getOrBuild();

            YarnProxy proxy = new YarnProxy();
            proxy.setYarnConfiguration(cfgs.getConfiguration());

            Map<String, String> yarnEnvironment = cfgs.getEnvironment();
            if(MapUtils.isNotEmpty(yarnEnvironment)) {
                proxy.setYarnEnvironment(yarnEnvironment);
            }
            ResourceLocalizer resourceLocalizer = cfgs.getLocalizer();
            if(null != resourceLocalizer) {
                proxy.setResourceLocalizer(resourceLocalizer);
            }
            BatchProxy batchProxy = batchConfig(cfg);
            if(null != batchProxy) {
                proxy.setBatchProxy(batchProxy);
            }
            YarnAppmaster yarnAppmaster = configAppMaster(cfg,cfgs,proxy);
            if(yarnAppmaster != null) {
                SpringYarnAppmasterProperties appmasterProperties = cfg.getAppmaster();
                AppmasterLauncherRunner runner = new AppmasterLauncherRunner();
                runner.setWaitLatch(appmasterProperties.isKeepContextAlive());
                runner.setContainerCount(appmasterProperties.getContainerCount());
                runner.setYarnAppmaster(yarnAppmaster);
                proxy.setAppmasterLauncherRunner(runner);
                proxy.setYarnAppmaster(yarnAppmaster);
                result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(YarnAppmaster.class).setBean(yarnAppmaster));
            }
            YarnContainer yarnContainer = configContainer(cfg,cfgs,proxy);
            if(yarnContainer != null) {
                ContainerLauncherRunner runner = new ContainerLauncherRunner();
                runner.setYarnContainer(yarnContainer);
                SpringYarnContainerProperties sycp = cfg.getContainer();
                if(null != sycp) {
                    runner.setWaitLatch(sycp.isKeepContextAlive());
                }
                proxy.setContainerLauncherRunner(runner);
                proxy.setYarnContainer(yarnContainer);
                result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(YarnContainer.class).setBean(yarnContainer));
            }
            YarnClient yarnClient = configClient(cfgs,proxy);
            if(yarnClient != null) {
                proxy.setYarnClient(yarnClient);
            }
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(YarnProxy.class).setBean(proxy));
        } catch (Exception e) {
            log.error("processRegisterBean error", e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public YarnContainer configContainer(SpringYarnProperties cfg,SpringYarnConfigs cfgs, YarnProxy proxy) {
        YarnContainer container = cfgs.getYarnContainer();
        if(container == null) {
            return null;
        }
        if (container instanceof AbstractYarnContainer) {
            ((AbstractYarnContainer) container).setBeanFactory(SpringUtil.getContext());
            ((AbstractYarnContainer) container).afterPropertiesSet();
        }
        if(container instanceof AbstractBatchYarnContainer) {
            DefaultMindAppmasterServiceClient client = new DefaultMindAppmasterServiceClient();
            client.setRequestChannel(new DirectChannel());
            client.setResponseChannel(new QueueChannel());
            ((AbstractBatchYarnContainer) container).setJobExplorer(new RemoteJobExplorer(client));
            ((AbstractBatchYarnContainer) container).setStepLocator(new BeanFactoryStepLocator());
            if(container instanceof DefaultBatchYarnContainer) {
                ((DefaultBatchYarnContainer) container).setAppmasterServiceClient(client);
            }
        }
        return container;
    }

    public YarnClient configClient(SpringYarnConfigs cfgs,YarnProxy proxy) {
        YarnClient client = cfgs.getYarnClient();
        if(client == null) {
            return null;
        }
//        if(client instanceof AbstractYarnClient) {
//            AbstractYarnClient abstractYarnClient = ((AbstractYarnClient) client);
//            abstractYarnClient.setConfiguration(proxy.getYarnConfiguration());
//            abstractYarnClient.setEnvironment(proxy.getYarnEnvironment());
//            abstractYarnClient.setResourceLocalizer(proxy.getResourceLocalizer());
//        }
        return client;
    }
    public YarnAppmaster configAppMaster(SpringYarnProperties cfg,SpringYarnConfigs cfgs,YarnProxy proxy) {
        YarnAppmaster yarnAppmaster = cfgs.getYarnAppmaster();
        if(yarnAppmaster == null) {
            return null;
        }
        // 不需要，已经做了
//        if (yarnAppmaster instanceof AbstractServicesAppmaster) {
//            ((AbstractServicesAppmaster) yarnAppmaster).setAllocator(new DefaultContainerAllocator());
//            ((AbstractServicesAppmaster) yarnAppmaster).setLauncher(new DefaultContainerLauncher());
//            ((AbstractServicesAppmaster) yarnAppmaster).setMonitor(new DefaultContainerMonitor());
//        }
        if (yarnAppmaster instanceof AbstractBatchAppmaster) {
            BatchProxy batchProxy = proxy.getBatchProxy();
            if(batchProxy == null) {
                throw new RuntimeException("you config appmasterClass with BatchAppmaster,but no batchRef Config");
            } else {
                YarnJobLauncher launcher = new YarnJobLauncher();
                launcher.setJobExplorer(batchProxy.getJobExplorer());
                launcher.setJobLauncher(batchProxy.getJobLauncher());
                launcher.setJobRegistry(batchProxy.getJobRegistry());
                launcher.setApplicationEventPublisher(applicationEventPublisher);
                ((AbstractBatchAppmaster) yarnAppmaster).setYarnJobLauncher(launcher);
                proxy.setYarnJobLauncher(launcher);
            }
        }
        SpringYarnHostInfoDiscoveryProperties syhidp = cfg.getHostdiscovery();
        if(syhidp != null && StringUtils.isNotBlank(syhidp.getMatchIpv4())) {
            DefaultHostInfoDiscovery discovery = new DefaultHostInfoDiscovery();
            discovery.setMatchIpv4(syhidp.getMatchIpv4());
            if (StringUtils.isNotBlank(syhidp.getMatchInterface())) {
                discovery.setMatchInterface(syhidp.getMatchInterface());
            }
            if (syhidp.getPreferInterface() != null) {
                discovery.setPreferInterface(syhidp.getPreferInterface());
            }
            discovery.setLoopback(syhidp.isLoopback());
            discovery.setPointToPoint(syhidp.isPointToPoint());
            EmbeddedAppmasterTrackService trackService = new EmbeddedAppmasterTrackService(discovery);
            if(yarnAppmaster instanceof AbstractAppmaster){
                ((AbstractAppmaster) yarnAppmaster).setAppmasterTrackService(trackService);
            }
        }
        SpringYarnAppmasterProperties appmasterProperties = cfg.getAppmaster();
        if (appmasterProperties.getContainercluster() != null && appmasterProperties.getContainercluster().getClusters() != null) {
            Map<String, SpringYarnAppmasterProperties.ContainerClustersProperties> clusterProps = appmasterProperties.getContainercluster().getClusters();
            if(yarnAppmaster instanceof AbstractContainerClusterAppmaster && MapUtils.isNotEmpty(clusterProps)){
                DefaultGridProjectionFactory factory = new DefaultGridProjectionFactory();
                GridProjectionFactoryRegistry registry = new GridProjectionFactoryRegistry();
                registry.addGridProjectionFactory(factory);
                ((AbstractContainerClusterAppmaster) yarnAppmaster).setGridProjectionFactoryLocator(registry);
                ((AbstractContainerClusterAppmaster) yarnAppmaster).setStateMachineFactory(SpringUtil.getBean(StateMachineFactory.class));
                Map<String, ProjectionData> projections = new HashMap<String, ProjectionData>();
                for (java.util.Map.Entry<String, SpringYarnAppmasterProperties.ContainerClustersProperties> entry : clusterProps.entrySet()) {
                    ProjectionData data = new ProjectionData();
                    SpringYarnAppmasterProperties.ContainerClustersProjectionProperties ccpProperties = entry.getValue().getProjection();
                    if (ccpProperties != null) {
                        SpringYarnAppmasterProperties.ContainerClustersProjectionDataProperties ccpdProperties = ccpProperties.getData();
                        if (ccpdProperties != null) {
                            data.setAny(ccpdProperties.getAny());
                            data.setHosts(ccpdProperties.getHosts());
                            data.setRacks(ccpdProperties.getRacks());
                            data.setProperties(ccpdProperties.getProperties());
                        }
                        data.setType(ccpProperties.getType());
                    }
                    SpringYarnAppmasterResourceProperties resource = entry.getValue().getResource();
                    if (resource != null) {
                        data.setPriority(resource.getPriority());
                        data.setVirtualCores(resource.getVirtualCores());
                        try {
                            data.setMemory(ParsingUtils.parseBytesAsMegs(resource.getMemory()));
                        } catch (ParseException e) {
                            log.error("configAppMaster error",e);
                        }
                    }
                    SpringYarnAppmasterLaunchContextProperties launchcontext = entry.getValue().getLaunchcontext();
                    if (launchcontext != null) {
                        data.setLocality(launchcontext.isLocality());
                    }
                    projections.put(entry.getKey(), data);
                }
                ((AbstractContainerClusterAppmaster) yarnAppmaster).setProjectionDataRegistry(new ProjectionDataRegistry(projections));
//                ((AbstractContainerClusterAppmaster) yarnAppmaster).setTaskScheduler(new ConcurrentTaskScheduler());
            }
        }

//        SpringYarnEnvProperties syep = cfg.getSyep();
//        if(StringUtils.isNotBlank(syep.getTrackUrl())) {
//            ContainerRegistrar containerRegistrar = new ContainerRegistrar(syep.getTrackUrl(), syep.getContainerId(), discovery);
//        }
        if (yarnAppmaster instanceof AbstractAppmaster) {
            ((AbstractAppmaster) yarnAppmaster).setBeanFactory(SpringUtil.getContext());
            ((AbstractAppmaster) yarnAppmaster).afterPropertiesSet();
        }
        return yarnAppmaster;
    }

    private BatchProxy batchConfig(SpringYarnProperties cfg){
        if(StringUtils.isBlank(cfg.getBatchRef())) {
            return null;
        }
        String batchBeanPrefix = YamlUtil.prefixToBeanName(PREFIX_BATCH + DOT + cfg.getBatchRef());
        BatchProxy batchProxy = SpringUtil.getBean(batchBeanPrefix + BEAN_BATCHPROXY, BatchProxy.class);
        if (batchProxy == null) {
            BatchProperties batchProperties = YamlUtil.configBind(PREFIX_BATCH, cfg.getBatchRef(), new BatchProperties());
            batchProxy = BatchAutoConfig.createConfig(batchProperties);
        }
        return batchProxy;
    }

    @Bean(name= YarnSystemConstants.DEFAULT_ID_EVENT_PUBLISHER)
    public YarnEventPublisher yarnEventPublisher() {
        DefaultYarnEventPublisher yarnEventPublisher = new DefaultYarnEventPublisher();
        yarnEventPublisher.setApplicationEventPublisher(applicationEventPublisher);
        return yarnEventPublisher;
    }

    @Bean
    public BootApplicationEventTransformer bootApplicationEventTransformer() {
        return new BootApplicationEventTransformer();
    }
    @Bean(name= YarnContextUtils.TASK_EXECUTOR_BEAN_NAME)
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        return executor;
    }
    @Bean(name= YarnContextUtils.TASK_SCHEDULER_BEAN_NAME)
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }
    //主要用来处理yarnComponent和onContainerStart等yarn的注解和生命周期？？
    @Bean(name="org.springframework.yarn.internal.springYarnAnnotationPostProcessor")
    public SpringYarnAnnotationPostProcessor springYarnAnnotationPostProcessor() {
        return new SpringYarnAnnotationPostProcessor();
    }

}
