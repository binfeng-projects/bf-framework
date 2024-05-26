package org.bf.framework.boot.constant;

/**
 * framework用到的一些常量
 */
public interface FrameworkConst {
    String DOT =".";
    String DEFAULT = "default";
    String FRAMEWORK_NAME = "bf";

    String PRIMARY = "primary";
    /**
     * 外界可传入你们业务对所有环境的命名规则，以免大规模改代码
     * 格式是逗号分隔，-Dbf.envs="local,develop,testing,pre,production"
     * 一定是按照本地，开发，测试，预发，生产这样的顺序。
     * 如果你恰好和框架命名默认规则一致，即"local,dev,test,pre,prod"
     * 那么什么都不用传
     */
    String INPUT_ENV_KEY = FRAMEWORK_NAME + DOT +"envs";
    String APP_NAME_KEY = "spring.application.name";

    String FRAMEWORK_KEY = FRAMEWORK_NAME +"-framework";
    String FRAMEWORK_VERSION_KEY = FRAMEWORK_KEY + DOT +"version";
    /**
     * 支持多集群环境，通过-Dbf.cluster 传入
     * 集群没法像env一样有个通用标准。框架默认有一个"default"集群，
     * 类似apollo等配置中心的default概念，传入后，也可以通过本工具类static方法获取
     * 以便业务做一些集群方面的逻辑。如果不传，就是"default"集群
     */
    String CURRENT_CLUSTER_KEY = FRAMEWORK_NAME + DOT +"cluster";
    String CURRENT_ENV_KEY = FRAMEWORK_NAME + DOT +"env";


    //-----------------------------配置文件相关----------------------------------
    /**
     * bf-framework-common-property
     * 框架默认的一些配置，注册到spring环境中的key
     * 例如连接池，健康检查端口等，可被配置中心覆盖，自然也可以被本地应用覆盖
     */
    String YML_FILE_EXTENSION = DOT +"yml";
    String YAML_FILE_EXTENSION = DOT +"yaml";
    String PROPERTY_FILE_EXTENSION = DOT +"properties";
    /**
     * classpath:bf-framework
     */
    /**
     * 三种文件按照顺序检查是否存在，找到就加载
     *  bf-framework.properties
     *  bf-framework.yaml
     *  bf-framework.yml
     */
    String[] SUPPORT_CONFIG_EXTENSION = new String[]{PROPERTY_FILE_EXTENSION,
            YAML_FILE_EXTENSION,YML_FILE_EXTENSION};


}
