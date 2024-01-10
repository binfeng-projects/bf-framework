package org.bf.framework.boot.util;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.config.SystemHelp;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.bf.framework.common.util.SystemUtil;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.*;
/**
 * 所有对当前应用环境信息的操作都应该收敛到这里(如果需要系统级别的环境变量，如OS啦，JDK版本啦，可参考{@link SystemUtil})
 * 可获取当前框架的版本
 * 可获取当前env,从spring.profiles.active获取，不支持多个profile,只取第一个,不传，默认dev
 * 可获取当前集群(cluster)，可-Dbf.cluster传入，如果不传，默认"default"集群
 * 可获取当前appName,从spring.application.name获取，必须配置，否则报错
 * 可获取Spring Environment暴露的api
 * 可操作binder
 * 可getBean
 */
@Slf4j
public class SpringUtil {
    public static final PathMatchingResourcePatternResolver RESOURCE_RESOLVER = new PathMatchingResourcePatternResolver();
    public static Object getBean(String name){
        return getBean(name,null);
    }
    public static <T> T getBean(Class<T> requiredType){
        return getBean(null,requiredType);
    }
    public static <T> T getBean(String name,Class<T> requiredType){
        try {
            if(StringUtils.isBlank(name)) {
                return ctx.getBean(requiredType);
            }
            return ctx.getBean(name,requiredType);
        } catch (Exception e) {
            log.error("get Bean error");
            return null;
        }
    }
    public static String currentEnv() {
        return CURRENT_ENV;
    }
    public static String frameworkVersion() {
        return FRAMEWORK_VERSION;
    }
    public static String appName() {
        return APP_NAME;
    }
    public static String currentCluster() {
        return CURRENT_CLUSTER;
    }
    public static boolean isDefaultCluster() {
        return DEFAULT.equals(CURRENT_CLUSTER);
    }
    /**
     * 注意名字是devOrTest,也包含local
     * @return
     */
    public static boolean devOrTest() {
        return isDev() || isTest() || isLocal();
    }
    public static boolean isLocal() {
        return SystemHelp.getEnvLocal().equals(CURRENT_ENV);
    }
    public static boolean isDev() {
        return SystemHelp.getEnvDev().equals(CURRENT_ENV);
    }
    public static boolean isTest() {
        return SystemHelp.getEnvTest().equals(CURRENT_ENV);
    }

    public static boolean isPre() {
        return SystemHelp.getEnvPre().equals(CURRENT_ENV);
    }
    public static boolean isProd() {
        return SystemHelp.getEnvProd().equals(CURRENT_ENV);
    }
    public static boolean preOrProd() {
        return isPre() || isProd();
    }
    public static boolean notProd() {
        return !isProd();
    }
    public static boolean notLocal() {
        return !isLocal();
    }

    /**
     * 普通的binder直接使用，封装一个类似工具类的静态方法，注意，至少需要EnvironmentAware生命周期之后使用。
     *
     */
    public static <T> T bind(String prefix, Class<T> clazz) {
        if(clazz == null) {
            return null;
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            return bind(prefix,instance);
        } catch (Exception e) {
            throw new RuntimeException("bind error, prefix : "+prefix+", target class : "+clazz,e);
        }
    }
    public static <T> T bind(String prefix,T result) {
        if(result == null) {
            return null;
        }
        ConfigurationPropertyName propertyName = ConfigurationPropertyName.EMPTY;
        if(StringUtils.isNotBlank(prefix)) {
            propertyName = ConfigurationPropertyName.of(prefix);
        }
        binder.bind(propertyName, Bindable.ofInstance(result));
        return result;
    }
    public static <T> T bindWithMap(String prefix, Class<T> clazz,Map<String,Object> map) {
        if(clazz == null || MapUtils.isEmpty(map)) {
            return null;
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            return bindWithMap(prefix,instance,map);
        } catch (Exception e) {
            throw new RuntimeException("bind error, prefix : "+prefix+", target class : "+clazz,e);
        }
    }
    public static <T> T bindWithMap(String prefix,T result,Map<String,Object> map) {
        if(result == null || MapUtils.isEmpty(map)) {
            return null;
        }
        ConfigurationPropertyName propertyName = ConfigurationPropertyName.EMPTY;
        if(StringUtils.isNotBlank(prefix)) {
            propertyName = ConfigurationPropertyName.of(prefix);
        }
        ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
        Binder binder = new Binder(source);
        binder.bind(propertyName, Bindable.ofInstance(result));
        return result;
    }
    public static void registrySingleton(Object bean){
        registrySingleton(bean.getClass().getName(),bean);
    }
    public static void registrySingleton(String beanName, Object bean){
        BeanRegisterUtil.registrySingleton(getContext().getBeanFactory(),beanName,bean);
    }
    //-----------------------系统的方法放在后面，上面的方法都是暴露使用的---------------------------
    private static ConfigurableApplicationContext ctx;
    private static ConfigurableEnvironment environment;
    public static ConfigurableEnvironment getEnvironment() {
        return environment;
    }
    public static ConfigurableApplicationContext getContext() {
        return ctx;
    }
    /**
     * 当前启动环境
     */
    private static String CURRENT_ENV;
    private static String FRAMEWORK_VERSION;
    private static String APP_NAME;
    private static String CURRENT_CLUSTER;
    public static void initFrameworkEnv(ConfigurableEnvironment env) {
        environment = env;
        binder = Binder.get(environment);
        SystemHelp.registerAllEnvs(env);
        //初始化当前环境
        CURRENT_ENV = SystemHelp.getEnvFromProfile(env);
        System.setProperty(CURRENT_ENV_KEY, CURRENT_ENV);
        //框架版本信息
        FRAMEWORK_VERSION = SpringUtil.class.getPackage().getImplementationVersion();
        System.setProperty(FRAMEWORK_VERSION_KEY,FRAMEWORK_VERSION);
        //appName
        String appName = env.getProperty(APP_NAME_KEY);
        if(StringUtils.isBlank(appName)){
            throw new RuntimeException("请配置" + APP_NAME_KEY);
        }
        APP_NAME = appName;
        //集群信息,默认default
        CURRENT_CLUSTER = System.getProperty(CURRENT_CLUSTER_KEY,DEFAULT);
        System.setProperty(CURRENT_CLUSTER_KEY,CURRENT_CLUSTER);
    }
    private static Binder binder;
    public static void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        ctx = applicationContext;
    }

}
