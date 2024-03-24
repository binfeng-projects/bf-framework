package org.bf.framework.boot.annotation.auto;

import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.YamlUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;

/**
 * 实现这个接口，可以定义注册bean和绑定属性，自动装配bean实例
 */
public interface EnableConfigHandler<T> {
    /**
     * 模版方法,拦截一些通用操作，会解析"prefix + .enable",获取所有的需要配置的资源名称
     * 另外也会检查是否有"prefix + .default" 配置,有的话也会传入子类注册
     * 另外会merge好default的配置信息，这样有default配置就会省很多配置
     * @param registry
     */
    default void registerBean(BeanDefinitionRegistry registry){
        if(registry == null){
            return;
        }
        String prefix = getPrefix();
        YamlUtil.parsePrefix(prefix,this::bindInstance,configMap -> {
            List<Middleware> middlewareList = processRegisterBean(configMap,registry);
            String schema = YamlUtil.getConfigSchema(configMap);
            Middleware.register(prefix + DOT + schema,middlewareList);
        });
    }

    /**
     * 子类需要实现的自定义注册逻辑，并且返回所有注册过的Bean，确保所有beanName全局唯一
     * @param registry
     */
    List<Middleware> processRegisterBean(Map<String,Object> properties,BeanDefinitionRegistry registry);

    String getPrefix();

    T bindInstance(Map<String,Object> properties);

//    default SslBundle getBundle(String bundleName){
//        if (StringUtils.isBlank(bundleName)) {
//            return null;
//        }
//        SslBundles bundles = SpringUtil.getBean(SslBundles.class);
//        if (bundles == null) {
//            return null;
//        }
//        return bundles.getBundle(bundleName);
//    }

}
