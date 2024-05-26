package org.bf.framework.autoconfigure.spi;

import org.bf.framework.boot.spi.EnvironmentConvertSystemProperty;
import org.bf.framework.common.util.MapUtils;

import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_SENTINEL;

public class DefaultEnviromentConvertSystemProperty implements EnvironmentConvertSystemProperty {

    //系统级别
    //enviroment中变量塞到System.setProperty中
    private static final Map<String, String> ENV_SYSTEM_PROPERTY_CONVERT = MapUtils.newHashMap();
    static {
        //key是三方中间件需要的key,v是我们配置文件配的key
        ENV_SYSTEM_PROPERTY_CONVERT.put("csp.sentinel.dashboard.server",PREFIX_SENTINEL + DOT + "dashboard");
    }
    @Override
    public Map<String, String> keyMaps() {
        return ENV_SYSTEM_PROPERTY_CONVERT;
    }
}
