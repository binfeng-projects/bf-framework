package org.bf.framework.boot.util;

import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.MethodMetadata;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 来自apollo com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil
 */
public class BeanRegisterUtil {
    // reserved bean definitions, we should consider drop this if we will upgrade Spring version
    private static final Map<String, String> RESERVED_BEAN_DEFINITIONS = new ConcurrentHashMap<>();

    static {
        RESERVED_BEAN_DEFINITIONS.put(
                "org.springframework.context.support.PropertySourcesPlaceholderConfigurer",
                "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration#propertySourcesPlaceholderConfigurer"
        );
    }
    public static BeanDefinition registry(BeanDefinitionRegistry r, Class<?> beanClass){
        return registry(r,beanClass.getName(),beanClass,-1);
    }
    public static BeanDefinition registry(BeanDefinitionRegistry r, Class<?> beanClass,int role){
        return registry(r,beanClass.getName(),beanClass,role);
    }
    public static BeanDefinition registry(BeanDefinitionRegistry r,String beanName, Class<?> beanClass){
        return registry(r,beanName,beanClass,-1);
    }
    public static BeanDefinition registry(BeanDefinitionRegistry r,String beanName,Class<?> beanClass,int role) {
        if(beanClass== null || StringUtils.isBlank(beanName)) {
            return null;
        }
        BeanDefinition bdf = registerBeanDefinitionIfNotExists(r,beanName, beanClass);
        if(null != bdf && role >= 0){
            bdf.setRole(role);
        }
        return bdf;
    }
    public static BeanDefinition registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, Class<?> beanClass) {
        return registerBeanDefinitionIfNotExists(registry, beanClass, null);
    }

    public static BeanDefinition registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, Class<?> beanClass,
                                                            Map<String, Object> extraPropertyValues) {
        return registerBeanDefinitionIfNotExists(registry, beanClass.getName(), beanClass, extraPropertyValues);
    }

    public static BeanDefinition registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                            Class<?> beanClass) {
        return registerBeanDefinitionIfNotExists(registry, beanName, beanClass, null);
    }

    public static BeanDefinition registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                            Class<?> beanClass, Map<String, Object> extraPropertyValues) {
        if (registry.containsBeanDefinition(beanName)) {
            return null;
        }
        String[] candidates = registry.getBeanDefinitionNames();
        String reservedBeanDefinition = RESERVED_BEAN_DEFINITIONS.get(beanClass.getName());
        for (String candidate : candidates) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
            if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
                return null;
            }
            if (reservedBeanDefinition != null && beanDefinition.getSource() != null && beanDefinition.getSource() instanceof MethodMetadata) {
                MethodMetadata metadata = (MethodMetadata) beanDefinition.getSource();
                if (Objects.equals(reservedBeanDefinition, String.format("%s#%s", metadata.getDeclaringClassName(), metadata.getMethodName()))) {
                    return null;
                }
            }
        }
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition();
        if (extraPropertyValues != null) {
            for (Map.Entry<String, Object> entry : extraPropertyValues.entrySet()) {
                beanDefinition.getPropertyValues().add(entry.getKey(), entry.getValue());
            }
        }
        registry.registerBeanDefinition(beanName, beanDefinition);
        return beanDefinition;
    }

    public static void registrySingleton(SingletonBeanRegistry r, Object bean){
        registrySingleton(r,bean.getClass().getName(),bean);
    }
    public static void registrySingleton(SingletonBeanRegistry r,String beanName, Object bean){
        if(bean == null || StringUtils.isBlank(beanName)) {
            return;
        }
        if(!r.containsSingleton(beanName)){
            r.registerSingleton(beanName, bean);
        }
    }
}
