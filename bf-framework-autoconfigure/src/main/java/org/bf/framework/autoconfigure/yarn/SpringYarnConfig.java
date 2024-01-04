package org.bf.framework.autoconfigure.yarn;

import org.bf.framework.autoconfigure.hadoop.HadoopProperties;
import org.bf.framework.autoconfigure.yarn.properties.*;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.util.StringUtils;
import org.springframework.data.hadoop.config.annotation.builders.HadoopConfigConfigurer;
import org.springframework.yarn.boot.support.BootLocalResourcesSelector;
import org.springframework.yarn.boot.support.BootMultiLocalResourcesSelector;
import org.springframework.yarn.boot.support.SpringYarnBootUtils;
import org.springframework.yarn.config.annotation.EnableYarn;
import org.springframework.yarn.config.annotation.SpringYarnConfigurerAdapter;
import org.springframework.yarn.config.annotation.builders.*;
import org.springframework.yarn.config.annotation.configurers.EnvironmentClasspathConfigurer;
import org.springframework.yarn.config.annotation.configurers.LocalResourcesCopyConfigurer;
import org.springframework.yarn.config.annotation.configurers.LocalResourcesHdfsConfigurer;
import org.springframework.yarn.config.annotation.configurers.MasterContainerAllocatorConfigurer;
import org.springframework.yarn.fs.LocalResourcesSelector;
import org.springframework.yarn.launch.LaunchCommandsFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_YARN;

public class SpringYarnConfig extends SpringYarnConfigurerAdapter {
    private final SpringYarnProperties yarnProperties;
    private final String DEFAULT_APPMASTER_CLASS = "org.springframework.yarn.batch.am.BatchAppmaster";

    private final String DEFAULT_CONTAINER_CLASS = "org.springframework.yarn.batch.container.DefaultBatchYarnContainer";
    public SpringYarnConfig(SpringYarnProperties yarnProperties) {
        this.yarnProperties = yarnProperties;
    }

    //  ================================appmaster=================================
    @Override
    public final void init(SpringYarnConfigBuilder builder) throws Exception {
        builder.setSharedObject(YarnConfigBuilder.class, getConfigBuilder());
        builder.setSharedObject(YarnResourceLocalizerBuilder.class, getLocalizerBuilder());
        builder.setSharedObject(YarnEnvironmentBuilder.class, getEnvironmentBuilder());

        if (shouldConfigContainer()) {
            builder.setSharedObject(YarnContainerBuilder.class, getContainerBuilder());
        }
        if (shouldConfigClient()) {
            builder.setSharedObject(YarnClientBuilder.class, getClientBuilder());
        } else if (shouldConfigAppMaster()) {
            builder.setSharedObject(YarnAppmasterBuilder.class, getAppmasterBuilder());
        }
    }

    @Override
    public void configure(YarnConfigConfigurer config) throws Exception{
        HadoopProperties shp = yarnProperties.getHadoopProperties();
        config.fileSystemUri(shp.getFsUri())
                .resourceManagerAddress(shp.getResourceManagerAddress())
                .schedulerAddress(shp.getResourceManagerSchedulerAddress())
                .withProperties()
                .properties(shp.getConfig())
                .and()
                .withResources()
                .resources(shp.getResources())
                .and()
                .withSecurity()
                .namenodePrincipal(shp.getSecurity() != null ? shp.getSecurity().getNamenodePrincipal() : null)
                .rmManagerPrincipal(shp.getSecurity() != null ? shp.getSecurity().getRmManagerPrincipal() : null)
                .authMethod(shp.getSecurity() != null ? shp.getSecurity().getAuthMethod() : null)
                .userPrincipal(shp.getSecurity() != null ? shp.getSecurity().getUserPrincipal() : null)
                .userKeytab(shp.getSecurity() != null ? shp.getSecurity().getUserKeytab() : null);
    }
    @Override
    public void configure(YarnAppmasterConfigurer master) throws Exception {
        if(!shouldConfigAppMaster()) {
            return;
        }
        SpringYarnAppmasterProperties appmasterProperties = yarnProperties.getAppmaster();
        SpringYarnAppmasterLaunchContextProperties appmasterLaunchContextProperties = appmasterProperties.getLaunchcontext();
        SpringYarnAppmasterResourceProperties appmasterResourceProperties = appmasterProperties.getResource();
        master.appmasterClass(appmasterProperties.getAppmasterClass() != null ? appmasterProperties.getAppmasterClass() : DEFAULT_APPMASTER_CLASS);
        if(appmasterLaunchContextProperties != null) {
             master.containerCommands(createCommands(appmasterLaunchContextProperties));
        }
        MasterContainerAllocatorConfigurer containerAllocatorConfigurer = master.withContainerAllocator();
        if(appmasterResourceProperties != null) {
            containerAllocatorConfigurer
                    .memory(appmasterResourceProperties.getMemory())
                    .priority(appmasterResourceProperties.getPriority())
                    .labelExpression(appmasterResourceProperties.getLabelExpression())
                    .virtualCores(appmasterResourceProperties.getVirtualCores());
        }
        if(appmasterLaunchContextProperties != null) {
            containerAllocatorConfigurer.locality(appmasterLaunchContextProperties.isLocality());
        }
        if (appmasterProperties.getContainercluster() != null && appmasterProperties.getContainercluster().getClusters() != null) {
            for (java.util.Map.Entry<String, SpringYarnAppmasterProperties.ContainerClustersProperties> entry : appmasterProperties.getContainercluster().getClusters().entrySet()) {
                SpringYarnAppmasterResourceProperties resource = entry.getValue().getResource();
                SpringYarnAppmasterLaunchContextProperties launchcontext = entry.getValue().getLaunchcontext();
                if(launchcontext != null) {
                    master.containerCommands(entry.getKey(), createCommands(launchcontext));
                }
                containerAllocatorConfigurer
                        .withCollection(entry.getKey())
                        .priority(resource != null ? resource.getPriority() : null)
                        .labelExpression(resource != null ? resource.getLabelExpression() : null)
                        .memory(resource != null ? resource.getMemory() : null)
                        .virtualCores(resource != null ? resource.getVirtualCores() : null)
                        .locality(launchcontext != null && launchcontext.isLocality());
            }
        }
    }
//  ================================yarnContainer=================================
    @Override
    public void configure(YarnContainerConfigurer container) throws Exception {
        if(!shouldConfigContainer()) {
            return;
        }
        SpringYarnContainerProperties containerProperties = yarnProperties.getContainer();
        if (StringUtils.isNotBlank(containerProperties.getContainerClass())) {
            container.containerClass(containerProperties.getContainerClass());
        } else {
            container.containerClass(DEFAULT_CONTAINER_CLASS);
        }
    }

    //  ================================client=================================
    @Override
    public void configure(YarnClientConfigurer client) throws Exception {
        if(!shouldConfigClient()) {
            return;
        }
        SpringYarnClientProperties clientProperties = yarnProperties.getClient();
        SpringYarnClientResourceProperties clientResourceProperties = clientProperties.getResource();
        SpringYarnClientLaunchContextProperties clientLaunchContextProperties = clientProperties.getLaunchcontext();
        client.clientClass(clientProperties.getClientClass())
                .appName(yarnProperties.getAppName())
                .appType(yarnProperties.getAppType())
                .priority(clientProperties.getPriority())
                .labelExpression(clientProperties.getLabelExpression())
                .queue(clientProperties.getQueue());
        if (null != clientResourceProperties) {
            client.memory(clientResourceProperties.getMemory())
                    .virtualCores(clientResourceProperties.getVirtualCores());
        }
        if (null != clientLaunchContextProperties) {
                client.masterCommands(createCommands(clientLaunchContextProperties));
        }
    }
    //  ================================client && appmaster=================================
    @Override
    public void configure(YarnResourceLocalizerConfigurer localizer) throws Exception {
        if(shouldConfigAppMaster()) {
            SpringYarnAppmasterProperties appmasterProperties = yarnProperties.getAppmaster();
            Map<String, LocalResourcesSelector> selectors = new HashMap<String, LocalResourcesSelector>();
            if (appmasterProperties.getContainercluster() != null && appmasterProperties.getContainercluster().getClusters() != null) {
                for (java.util.Map.Entry<String, SpringYarnAppmasterProperties.ContainerClustersProperties> entry : appmasterProperties.getContainercluster().getClusters().entrySet()) {
                    SpringYarnAppmasterLocalizerProperties props = entry.getValue().getLocalizer();
                    if (props == null) {
                        continue;
                    }
                    BootLocalResourcesSelector selector = new BootLocalResourcesSelector(BootLocalResourcesSelector.Mode.CONTAINER);
                    if (StringUtils.isNotBlank(props.getZipPattern())) {
                        selector.setZipArchivePattern(props.getZipPattern());
                    }
                    if (props.getPropertiesNames() != null) {
                        selector.setPropertiesNames(props.getPropertiesNames());
                    }
                    if (props.getPropertiesSuffixes() != null) {
                        selector.setPropertiesSuffixes(props.getPropertiesSuffixes());
                    }
                    selector.addPatterns(props.getPatterns());
                    selectors.put(entry.getKey(), selector);
                }
            }
            BootLocalResourcesSelector selector = new BootLocalResourcesSelector(BootLocalResourcesSelector.Mode.CONTAINER);
            SpringYarnAppmasterLocalizerProperties appmasterLocalizerProperties = appmasterProperties.getLocalizer();
            if(appmasterLocalizerProperties != null) {
                if (StringUtils.isNotBlank(appmasterLocalizerProperties.getZipPattern())) {
                    selector.setZipArchivePattern(appmasterLocalizerProperties.getZipPattern());
                }
                if (appmasterLocalizerProperties.getPropertiesNames() != null) {
                    selector.setPropertiesNames(appmasterLocalizerProperties.getPropertiesNames());
                }
                if (appmasterLocalizerProperties.getPropertiesSuffixes() != null) {
                    selector.setPropertiesSuffixes(appmasterLocalizerProperties.getPropertiesSuffixes());
                }
                selector.addPatterns(appmasterLocalizerProperties.getPatterns());
            }
            BootMultiLocalResourcesSelector localResourcesSelector = new BootMultiLocalResourcesSelector(selector, selectors);
            String applicationDir = resolveApplicationdir(yarnProperties);
            localizer.stagingDirectory(yarnProperties.getStagingDir());
            LocalResourcesHdfsConfigurer withHdfs = localizer.withHdfs();
            for (LocalResourcesSelector.Entry e : localResourcesSelector.select(applicationDir != null ? applicationDir : "/")) {
                withHdfs.hdfs(e.getPath(), e.getType(), applicationDir == null);
            }

            if (appmasterProperties.getContainercluster() != null && appmasterProperties.getContainercluster().getClusters() != null) {
                for (java.util.Map.Entry<String, SpringYarnAppmasterProperties.ContainerClustersProperties> entry : appmasterProperties.getContainercluster().getClusters().entrySet()) {
                    withHdfs = localizer.withHdfs(entry.getKey());
                    for (LocalResourcesSelector.Entry e : localResourcesSelector.select(entry.getKey(), applicationDir != null ? applicationDir : "/")) {
                        withHdfs.hdfs(e.getPath(), e.getType(), applicationDir == null);
                    }
                }
            }
        }
        if(shouldConfigClient()) {
            SpringYarnClientProperties clientProperties = yarnProperties.getClient();
            BootLocalResourcesSelector selector = new BootLocalResourcesSelector(BootLocalResourcesSelector.Mode.APPMASTER);
            String applicationDir = resolveApplicationdir(yarnProperties);
            LocalResourcesCopyConfigurer copyConfigurer =localizer.stagingDirectory(yarnProperties.getStagingDir())
                    .withCopy()
                    .copy(org.springframework.util.StringUtils.toStringArray(clientProperties.getFiles()), applicationDir, applicationDir == null);
            LocalResourcesHdfsConfigurer withHdfs = localizer.withHdfs();
            for (LocalResourcesSelector.Entry e : selector.select(applicationDir != null ? applicationDir : "/")) {
                withHdfs.hdfs(e.getPath(), e.getType(), applicationDir == null);
            }
            SpringYarnClientLocalizerProperties clientLocalizerProperties = clientProperties.getLocalizer();
            if(clientLocalizerProperties != null) {
                copyConfigurer.raw(unescapeMapKeys(clientLocalizerProperties.getRawFileContents()), applicationDir);
                if (StringUtils.isNotBlank(clientLocalizerProperties.getZipPattern())) {
                    selector.setZipArchivePattern(clientLocalizerProperties.getZipPattern());
                }
                if (clientLocalizerProperties.getPropertiesNames() != null) {
                    selector.setPropertiesNames(clientLocalizerProperties.getPropertiesNames());
                }
                if (clientLocalizerProperties.getPropertiesSuffixes() != null) {
                    selector.setPropertiesSuffixes(clientLocalizerProperties.getPropertiesSuffixes());
                }
                selector.addPatterns(clientLocalizerProperties.getPatterns());
            }
        }
    }
    private static Map<String, byte[]> unescapeMapKeys(Map<String, byte[]> map) {
        if (map == null || map.isEmpty()) {
            return map;
        }
        HashMap<String, byte[]> nmap = new HashMap<String, byte[]>();
        for (String key : map.keySet()) {
            nmap.put(SpringYarnBootUtils.unescapeConfigKey(key), map.get(key));
        }
        return nmap;
    }

    public boolean shouldConfigAppMaster(){
        String yarnMode = SpringUtil.getEnvironment().getProperty(PREFIX_YARN + DOT + "mode");
        boolean masterMode = EnableYarn.Enable.APPMASTER.name().equals(yarnMode);
        SpringYarnAppmasterProperties appmasterProperties = yarnProperties.getAppmaster();
        return masterMode && appmasterProperties != null && StringUtils.isNotBlank(appmasterProperties.getAppmasterClass());
    }
    public boolean shouldConfigClient(){
        String yarnMode = SpringUtil.getEnvironment().getProperty(PREFIX_YARN + DOT + "mode");
        boolean clientMode = StringUtils.isBlank(yarnMode) || yarnMode.equals(EnableYarn.Enable.CLIENT.name());
        SpringYarnClientProperties clientProperties = yarnProperties.getClient();
        return clientMode && clientProperties != null && StringUtils.isNotBlank(clientProperties.getClientClass());
    }
    public boolean shouldConfigContainer(){
        String yarnMode = SpringUtil.getEnvironment().getProperty(PREFIX_YARN + DOT + "mode");
        //如果是master也启动container ?
        boolean containerModel =  EnableYarn.Enable.CONTAINER.name().equals(yarnMode);
        SpringYarnContainerProperties containerProperties = yarnProperties.getContainer();
        return containerModel && containerProperties != null && StringUtils.isNotBlank(containerProperties.getContainerClass());
    }
    @Override
    public void configure(YarnEnvironmentConfigurer environment) throws Exception {
        if(shouldConfigAppMaster()) {
            SpringYarnAppmasterProperties appmasterProperties = yarnProperties.getAppmaster();
            configureEnv(environment,appmasterProperties.getLaunchcontext(),null);
            if (appmasterProperties.getContainercluster() != null && appmasterProperties.getContainercluster().getClusters() != null) {
                for (java.util.Map.Entry<String, SpringYarnAppmasterProperties.ContainerClustersProperties> entry : appmasterProperties.getContainercluster().getClusters().entrySet()) {
                    configureEnv(environment,entry.getValue().getLaunchcontext(),entry.getKey());
                }
            }
        }
        if(shouldConfigClient()) {
            SpringYarnClientProperties clientProperties = yarnProperties.getClient();
            configureEnv(environment,clientProperties.getLaunchcontext(),null);
        }
    }

    public EnvironmentClasspathConfigurer configureEnv(YarnEnvironmentConfigurer environment, AbstractLaunchContextProperties contextProperties, String classPathId) {
        EnvironmentClasspathConfigurer result = null;
        try {
            if(contextProperties != null) {
                environment = environment.includeLocalSystemEnv(contextProperties.isIncludeLocalSystemEnv());
            }
            if(StringUtils.isBlank(classPathId)) {
                result = environment.withClasspath();
            } else {
                result = environment.withClasspath(classPathId);
            }
            result = result.siteYarnAppClasspath(yarnProperties.getSiteYarnAppClasspath())
                    .siteMapreduceAppClasspath(yarnProperties.getSiteMapreduceAppClasspath());

            if(contextProperties != null) {
                result = result.includeBaseDirectory(contextProperties.isIncludeBaseDirectory())
                        .useYarnAppClasspath(contextProperties.isUseYarnAppClasspath())
                        .useMapreduceAppClasspath(contextProperties.isUseMapreduceAppClasspath())
                        .delimiter(contextProperties.getPathSeparator())
                        .entries(contextProperties.getContainerAppClasspath())
                        .entry(explodedEntryIfZip(contextProperties));
            }
            return result;
        } catch (Exception e) {
             return null;
        }
    }
    public static String explodedEntryIfZip(AbstractLaunchContextProperties syalcp) {
        return StringUtils.endsWithIgnoreCase(syalcp.getArchiveFile(), ".zip") ? "./" + syalcp.getArchiveFile() : null;
    }

    public static String[] createCommands(AbstractLaunchContextProperties syalcp) throws Exception {
//        if(true) {
//            return new String[]{"ls -lha"};
//        }
        if(syalcp == null) {
            return null;
        }
        LaunchCommandsFactoryBean factory = new LaunchCommandsFactoryBean();
        String jarFile = syalcp.getArchiveFile();
        if (StringUtils.isNotBlank(jarFile) && jarFile.endsWith("jar")) {
            factory.setJarFile(jarFile);
        } else if (StringUtils.isNotBlank(syalcp.getRunnerClass())) {
            factory.setRunnerClass(syalcp.getRunnerClass());
        } else if (StringUtils.isNotBlank(jarFile) && jarFile.endsWith("zip")) {
            factory.setRunnerClass("org.springframework.boot.loader.PropertiesLauncher");
        }
        if(StringUtils.isNotBlank(syalcp.getCommand())) {
            factory.setCommand(syalcp.getCommand());
        }
        factory.setArgumentsList(syalcp.getArgumentsList());
        if (syalcp.getArguments() != null) {
            Properties arguments = new Properties();
            arguments.putAll(syalcp.getArguments());
            factory.setArguments(arguments);
        }
        factory.setOptions(syalcp.getOptions());
        factory.setStdout("<LOG_DIR>/Container.stdout");
        factory.setStderr("<LOG_DIR>/Container.stderr");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    public static String resolveApplicationdir(SpringYarnProperties syp) {
        if (StringUtils.isNotBlank(syp.getApplicationBaseDir()) && StringUtils.isNotBlank(syp.getApplicationVersion())) {
            return (syp.getApplicationBaseDir().endsWith("/") ? syp.getApplicationBaseDir() : syp
                    .getApplicationBaseDir() + "/")
                    + syp.getApplicationVersion() + "/";
        } else {
            String dir = syp.getApplicationDir();
            if (StringUtils.isNotBlank(dir) && !dir.endsWith("/")) {
                dir = dir + "/";
            }
            return dir;
        }
    }
}