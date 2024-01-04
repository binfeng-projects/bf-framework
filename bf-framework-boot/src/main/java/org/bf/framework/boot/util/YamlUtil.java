package org.bf.framework.boot.util;

import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

/**
 * 解析prefix成map，会解析"prefix + .enable",获取所有的需要配置的资源名称
 * 另外也会检查是否有"prefix + .default" 配置,有的话也会传入子类注册
 * 另外会merge好default的配置信息，这样有default配置就会省很多配置
 */
public class YamlUtil {
    public static final String CODE_GEN = "codegen";
    public static final String CODE_GEN_INCLUDE = CODE_GEN + "-include";
    //其他例如elasticsearch等的Repository生成代码也走mysql表，统一收敛到mysql jooqgen
    public static final String CODE_GEN_REF = CODE_GEN + "-ref";
    public static final String CODE_GEN_EXCLUDE = CODE_GEN + "-exclude";

    /**
     * 循环中回调的prefix
     */
    public static final String CONFIG_SCHEMA = "bf_config_schema";
    /**
     * 循环中回调的次数（当前第几次）
     */
    public static final String CONFIG_CALLBACK = "bf_config_callback_time";
    /**
     * bind的对象实例
     */
    public static final String CONFIG_BIND = "configBind";
    /**
     * 请确保在spring容器环境中执行，测试环境请保证springboottest
     * @param prefix 配置prefix
     */
    public static <Bind> void parsePrefix(String prefix, Function<Map<String,Object>,Bind> mapToBindFunc, Consumer<Map<String,Object>> func){
        int callBack = 0;
        Map<String,Object> defualtConfig = MapUtils.newHashMap();
        SpringUtil.bind(prefix + DOT + DEFAULT, defualtConfig);
        //检查url这个key有没有值，如果有，认为需要实例化资源
        if(StringUtils.isNotBlank((String)defualtConfig.get(URL))) {
            callBack++;
            defualtConfig.put(CONFIG_CALLBACK,callBack);
            defualtConfig.put(CONFIG_BIND,configBind(prefix,DEFAULT,mapToBindFunc,defualtConfig));
            defualtConfig.put(CONFIG_SCHEMA,DEFAULT);
            func.accept(defualtConfig);
        }
        String enabledPrefix = prefix + DOT + ENABLED;
        String enabledValue = SpringUtil.getEnvironment().getProperty(enabledPrefix);
        if (StringUtils.isBlank(enabledValue)){
            return;
        }
        String[] resources = enabledValue.split(",");
        for (String resource : resources) {
            Map<String,Object> resourceConfig = MapUtils.newHashMap();
            SpringUtil.bind(prefix + DOT + resource.trim(), resourceConfig);;
            if(MapUtils.isEmpty(resourceConfig)) {
                continue;
            }
            //具体配置回调
            callBack++;
            resourceConfig = MapUtils.mergeMaps(resourceConfig,defualtConfig,false);
            resourceConfig.put(CONFIG_CALLBACK,callBack);
            resourceConfig.put(CONFIG_BIND,configBind(prefix,resource.trim(),mapToBindFunc,resourceConfig));
            resourceConfig.put(CONFIG_SCHEMA,prefixToBeanName(resource.trim()));
            func.accept(resourceConfig);
        }
    }

    /**
     * @param prefix 一般为中间件类型
     * @param schema 具体实例，和prefix一起会组成spring解析property的真正prefix
     * @param mapToBindFunc 特殊情况下，会需要复杂的方法生成bindInstance,例如jdbcAutoConfig中的datasource
     * @param inputMap 可为空，如果为空，内部自己从配置文件解析
     * @return
     * @param <Bind>
     */
    public static <Bind> Bind configBind(String prefix,String schema,Function<Map<String,Object>,Bind> mapToBindFunc,Map<String,Object> inputMap){
        if(StringUtils.isBlank(prefix) || StringUtils.isBlank(schema)) {
            return null;
        }
        if(inputMap == null) {
            inputMap = MapUtils.newHashMap();
            String prefixSchema = prefix + DOT + schema;
            SpringUtil.bind(prefixSchema, inputMap);
            if(MapUtils.isEmpty(inputMap)) { //如果有默认配置，那么检查
                return null;
            }
        }
        if(null == mapToBindFunc) {
            return null;
        }
        return configBind(prefix,schema,mapToBindFunc.apply(inputMap));
    }

    public static <Bind> Bind configBind(String prefix,String schema,Bind b){
        if(StringUtils.isBlank(prefix) || StringUtils.isBlank(schema)) {
            return null;
        }
        if(b == null) {
            return null;
        }
        if(!DEFAULT.equals(schema)) {
            SpringUtil.bind(prefix + DOT + DEFAULT, b);
        }
        String prefixSchema = prefix + DOT + schema;
        SpringUtil.bind(prefixSchema, b);
        return b;
    }
    public static String getConfigSchema(Map<String,Object> configMap){
        if(MapUtils.isEmpty(configMap)) {
            return null;
        }
        return (String)configMap.get(CONFIG_SCHEMA);
    }
    public static int getConfigCallBack(Map<String,Object> configMap){
        if(MapUtils.isEmpty(configMap)) {
            return -1;
        }
        return (int)configMap.get(CONFIG_CALLBACK);
    }

    public static boolean firstCallBack(Map<String,Object> configMap){
        if(MapUtils.isEmpty(configMap)) {
            return false;
        }
        return getConfigCallBack(configMap) == 1;
    }
    public static Object getConfigBind(Map<String,Object> configMap){
        if(MapUtils.isEmpty(configMap)) {
            return null;
        }
        return configMap.get(CONFIG_BIND);
    }
    public static String prefixToBeanName(String input) {
        return input.replace('-', '_');
    }

    public static String beanNameToPrefix(String input) {
        return input.replace('_','-');
    }

}
