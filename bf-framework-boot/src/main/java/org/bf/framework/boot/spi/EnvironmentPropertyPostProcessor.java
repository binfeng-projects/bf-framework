package org.bf.framework.boot.spi;

import org.springframework.core.Ordered;

import java.util.Map;

public interface EnvironmentPropertyPostProcessor extends Ordered {
    /**
     * classPathConfigFileKey为空或者未加载到配置，才会执行这个方法
     * @return
     */
    default Map<String,Object> simpleProperties() {
        return null;
    }
    /**
     * classpath中的配置文件名，不需要带扩展名，会自动检查.yaml .property .yml三种格式
     * 三种文件按照顺序检查是否存在，找到就加载
     *  .properties
     *  .yaml
     *  .yml
     *  如果这个返回值有值，且配置文件都真实存在，那么就会忽略simpleProperties的返回值
     *  加载逻辑，类似于spring加载application-xxx.yml的逻辑
     *  //        先加载公共部分configFileKey.yml(property)，类似于application.yml
     *         MapPropertySource propertySource = parseClassPathConfig(configFileKey);
     *         if(null != propertySource) {
     *             result.putAll(propertySource.getSource());
     *         }
     *         String currentEnv = SpringUtil.currentEnv();
     * //        再先加载环境分支configFileKey-env.yml(property)，类似于application-env.yml
     *         propertySource = parseClassPathConfig(configFileKey + "-" +currentEnv);
     *         if(null != propertySource) {
     *             result.putAll(propertySource.getSource());
     *         }
     *         String currentCluster = SpringUtil.currentCluster();
     * //  再先加载环境分支configFileKey-env-cluster.yml(property)，类似于application-env-cluster.yml
     */
    default String classPathConfigFileKey() {
        return null;
    }
    @Override
    default int getOrder() {
        return 0;
    }
}
