package org.bf.framework.boot.util;

import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bf.framework.boot.constant.FrameworkConst.DEFAULT;
import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.ENABLED;
import static org.bf.framework.boot.constant.MiddlewareConst.URL;

public class CommandLineUtil {
    public static final String PASS_STR = "passcheck";
    public static final String BLANK = "  ";
    public static Map<String,String> resolveArgs(String ...args){
        //解析spring参数
        DefaultApplicationArguments springArgs = new DefaultApplicationArguments(args);
        Set<String> optionNames = springArgs.getOptionNames();
        Map<String,String> result = MapUtils.newHashMap();
        for (String optionName : optionNames) {
            List<String> optionValues = springArgs.getOptionValues(optionName);
            if (CollectionUtils.isEmpty(optionValues)) {
                continue;
            }
            result.put(optionName, String.join(",", optionValues));
        }
        return result;
    }

    /**
     * 只配置指定启动的配置项，如果没有指定，取default
     * 总之，全局只有一个生效
     * @param prefixSingleConfig
     * @return
     */
    public static List<String> argActiveSingleSchema(String prefixSingleConfig,String schema,Map<String,String> argMap){
        if(argMap == null || StringUtils.isBlank(prefixSingleConfig)){
            return null;
        }
        //拼接例如：--bf.yarn.default.url=xxx
        //框架逻辑只要url这个key不为空（可以是任意值）。就激活default配置
        String defaultUrlKey = prefixSingleConfig + DOT + DEFAULT + DOT + URL;
        String enabledKey = prefixSingleConfig + DOT + ENABLED;
        String enabledValue = argMap.get(enabledKey);
        if(StringUtils.isBlank(schema)){ //如果代码指定的schema为空，则先设置为default
            argMap.put(prefixSingleConfig + DOT + ENABLED,BLANK); //让enabled失效
            argMap.put(defaultUrlKey,PASS_STR); //让default通过
        } else {
            argMap.put(enabledKey,schema); //覆盖回去
            argMap.put(defaultUrlKey,BLANK); //让default失效
        }
//        命令行的优先级是最高的
        if(StringUtils.isNotBlank(enabledValue)) {
            String first = enabledValue.split(",")[0];
            //因为只允许启动一个实例，所以取第一个
            if(StringUtils.isBlank(first) || DEFAULT.equals(first)){
                argMap.put(prefixSingleConfig + DOT + ENABLED,BLANK); //让enabled失效
                argMap.put(defaultUrlKey,PASS_STR); //让default通过
            } else {
                argMap.put(enabledKey,first); //覆盖回去
                argMap.put(defaultUrlKey,BLANK); //让default失效
            }
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String,String> entry : argMap.entrySet()) {
            if(StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())) {
                continue;
            }
            result.add("--" + entry.getKey() + "=" + entry.getValue());
        }
        return result;
    }
    /**
     * 只配置指定启动的配置项，如果没有指定，取default
     * 总之，全局只有一个生效
     * @return
     */
    public static List<String> argActiveSingleSchema(Map<String,String> prefixSchemaMap,String[] args){
        Map<String,String> paramMap = resolveArgs(args);
        if(MapUtils.isNotEmpty(prefixSchemaMap)) {
            for (Map.Entry<String,String> entry : prefixSchemaMap.entrySet()) {
                argActiveSingleSchema(entry.getKey(), entry.getValue(),paramMap);
            }
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String,String> entry : paramMap.entrySet()) {
            result.add("--" + entry.getKey() + "=" + entry.getValue());
        }
        return result;
    }
}
