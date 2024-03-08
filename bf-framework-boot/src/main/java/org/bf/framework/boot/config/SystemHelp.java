package org.bf.framework.boot.config;

import cn.hutool.core.util.ServiceLoaderUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.spi.EnvironmentPropertyPostProcessor;
import org.bf.framework.boot.util.SpringInjector;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.base.IdStringEnum;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@Slf4j
public class SystemHelp {
    //所有环境的枚举
    private static final Map<EnvEnum, String> ENV_MAP = MapUtils.newHashMap();

    //enviroment中变量塞到System.setProperty中
    private static final Map<String, String> ENV_SYSTEM_PROPERTY_CONVERT = MapUtils.newHashMap();
    static {
        //key是三方中间件需要的key,v是我们配置文件配的key
        ENV_SYSTEM_PROPERTY_CONVERT.put("csp.sentinel.dashboard.server",PREFIX_SENTINEL + DOT + "dashboard");
    }
    /**
     * 框架并不知道外界对环境的命名规则，虽然标准上大家应该都分本地，dev,test,pre,prod五个环境，但是叫法可能不一样
     * 支持-D传入告诉框架，框架会针对不同环境做些特殊逻辑，比如很多时候dev,test方便测试会不做鉴权
     * 格式是逗号分隔，-Dbf.envs="local,develop,testing,pre,production"
     * 一定是按照本地，开发，测试，预发，生产这样的顺序。
     */
    public static void registerAllEnvs(ConfigurableEnvironment env) {
        String allInputEnvs = System.getProperty(INPUT_ENV_KEY);
        //如果外界没指定，那默认就是dev=开发环境，test=测试环境，pre=预发环境，prod=生产环境
        EnvEnum[] allDefaultEnvs = EnvEnum.values();
        if (StringUtils.isBlank(allInputEnvs)) {
            for (EnvEnum e : allDefaultEnvs) {
                ENV_MAP.put(e, e.getId());
            }
            System.setProperty(INPUT_ENV_KEY,StringUtils.join(",",ENV_MAP.values()));
        } else {
            String[] inputEnvs = allInputEnvs.split(",");
            //简单处理，命名上支持自定义，环境数量就只支持特定个了。
            if (inputEnvs.length < allDefaultEnvs.length) {
                throw new RuntimeException("环境数量少于系统设定，请按照规范传入");
            }
            for (int i = 0; i < allDefaultEnvs.length; i++) {
                ENV_MAP.put(allDefaultEnvs[i], inputEnvs[i]);
            }
        }
    }
    /**
     * 注册一些key到systemProperty中
     */
    public static void registerSystemProperty(ConfigurableEnvironment env) {
        //sentinel
        System.setProperty("project.name", env.getProperty("project.name", SpringUtil.appName()));
        for (Map.Entry<String,String> entry : ENV_SYSTEM_PROPERTY_CONVERT.entrySet()) {
            String cfgValue = env.getProperty(entry.getValue()); //例如，bf.sentinel.dashboard
            if(StringUtils.isNotBlank(cfgValue)) {
                System.setProperty(entry.getKey(), cfgValue);
            }
        }
    }
    /**
     * 注册一些key到Environment中
     */
    public static void registerEnvironmentProperty(ConfigurableEnvironment env) {
        List<EnvironmentPropertyPostProcessor> list = ServiceLoaderUtil.loadList(EnvironmentPropertyPostProcessor.class);
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.sort(Comparator.comparingInt(Ordered::getOrder));
        for (EnvironmentPropertyPostProcessor processor: list) {
            Map<String,Object> property = loadIfConfigFileProperty(processor.classPathConfigFileKey());
            if(MapUtils.isEmpty(property)) {
                property = processor.simpleProperties();
            }
            if(MapUtils.isEmpty(property)) {
                continue;
            }
            MapPropertySource source = new MapPropertySource(processor.getClass().getName(), property);
            env.getPropertySources().addLast(source);
        }
        //加载框架默认配置，例如一些连接池的默认配置等等
        PropertySource<?> commonCfg = parseClassPathConfig(FRAMEWORK_KEY);
        env.getPropertySources().addLast(commonCfg);
    }
//    /**
//     * 加载一些key到systemProperty中
//     * @param systemPropertyYamlConfigPrefixKey
//     */
//    public static Map<String,Object> loadIfSystemProperty(ConfigurableEnvironment env,String systemPropertyYamlConfigPrefixKey) {
//        Map<String,Object> result = MapUtils.newHashMap();
//        List<String> props = env.getProperty(PREFIX + DOT + systemPropertyYamlConfigPrefixKey, List.class);
//        if(CollectionUtils.isEmpty(props)) {
//            return result;
//        }
//        for (String p : props) {
//            String[] kv = p.trim().split("=");
//            //systemProperty优先，如果没有才用配置文件的
//            result.put(kv[0],System.getProperty(kv[0],kv[1]));
//        }
//        return result;
//    }
    /**
     * 加载一些key到Environment中
     * @param configFileKey
     */
    public static Map<String,Object> loadIfConfigFileProperty(String configFileKey) {
        Map<String,Object> result = MapUtils.newHashMap();
        if(StringUtils.isBlank(configFileKey)) {
            return result;
        }
//        先加载公共部分，类似于application.yml
        result.putAll(convertYamlConfig(configFileKey));
//        再先加载环境分支，类似于application-env.yml(也相当于application-env-default集群.yml)
        String currentEnv = SpringUtil.currentEnv();
        result.putAll(convertYamlConfig(configFileKey + "-" +currentEnv));
//        再先加载集群分支，类似于application-env-cluster.yml
        String currentCluster = SpringUtil.currentCluster();
        result.putAll(convertYamlConfig(configFileKey + "-" +currentEnv + "-" + currentCluster));
        return result;
    }
    public static Map<String,Object> convertYamlConfig(String configFileKey) {
        Map<String,Object> result = MapUtils.newHashMap();
        if(StringUtils.isBlank(configFileKey)) {
            return result;
        }
//        先加载公共部分，类似于application.yml
        PropertySource<?> propertySource = parseClassPathConfig(configFileKey);
        if(null != propertySource) {
            Map<String, OriginTrackedValue> sourceMap = (Map<String, OriginTrackedValue>)propertySource.getSource();
            for (Map.Entry<String,OriginTrackedValue> entry : sourceMap.entrySet()) {
                //OriginTrackedValue.getValue，才是原始值
                result.put(entry.getKey(),String.valueOf(entry.getValue().getValue()));
            }
        }
        return result;
    }
    /**
     * @param fileNameWithOutExtension classpath中的配置文件名，不需要带扩展名，会自动检查.yaml .property .yml三种格式
     * 三种文件按照顺序检查是否存在，找到就加载
     *  .properties
     *  .yaml
     *  .yml
     */
    public static PropertySource<?> parseClassPathConfig(String fileNameWithOutExtension){
        CommonFileSourceFactory factory = SpringInjector.getInstance(CommonFileSourceFactory.class);
        for (String ext : SUPPORT_CONFIG_EXTENSION){
            try {
                EncodedResource source = new EncodedResource(new ClassPathResource(fileNameWithOutExtension + ext));
                PropertySource<?>  propertySource = factory.createPropertySource(fileNameWithOutExtension,source);
                if(propertySource != null){
                    log.info("load common config " + fileNameWithOutExtension + ext);
                    return propertySource;
                }
            } catch (Exception e){
            }
        }
        return null;
    }
    public static String getEnvLocal() {
        return ENV_MAP.get(EnvEnum.LOCAL);
    }
    public static String getEnvDev() {
        return ENV_MAP.get(EnvEnum.DEV);
    }
    public static String getEnvTest() {
        return ENV_MAP.get(EnvEnum.TEST);
    }

    public static String getEnvPre() {
        return ENV_MAP.get(SystemHelp.EnvEnum.PRE);
    }
    public static String getEnvProd() {
        return ENV_MAP.get(EnvEnum.PROD);
    }

    public static String getEnvFromProfile(ConfigurableEnvironment env) {
        String[] profiles = env.getActiveProfiles();
        // 默认dev环境
        if (profiles.length == 0) {
            return ENV_MAP.get(EnvEnum.DEV);
        }
        return profiles[0];
    }
    @AllArgsConstructor
    @Getter
    private enum EnvEnum implements IdStringEnum {
        LOCAL("local"), DEV("dev"),TEST("test"),PRE("pre"),PROD("prod");
        private String id;
    }

}
