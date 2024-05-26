package org.bf.framework.boot.spi;

import org.springframework.core.Ordered;

import java.util.Map;

public interface EnvironmentConvertSystemProperty extends Ordered {
    /**
     * key SystemProperty中准备塞入的key
     * value SpringEnviroment中的key
     * System.setProperty(entry.getKey(), env.getProperty(entry.getValue()));
     */
    Map<String,String> keyMaps();

    @Override
    default int getOrder() {
        return 0;
    }
}
