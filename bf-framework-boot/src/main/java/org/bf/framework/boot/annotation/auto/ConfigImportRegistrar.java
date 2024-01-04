package org.bf.framework.boot.annotation.auto;

import org.bf.framework.boot.util.SpringUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.InvocationTargetException;

public class ConfigImportRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableConfig.class.getName()));
        //实际的注册逻辑交给具体的handler
        Class<? extends EnableConfigHandler> cls = attributes.getClass("value");
        try {
            EnableConfigHandler handler = null;
            try {
                handler = SpringUtil.getBean(cls);
            } catch (Exception e) {
                //ignore
            }
            if(handler == null) {
                handler = cls.getDeclaredConstructor().newInstance();
            }
            //得到注册的所有beanName，确保全局唯一
            handler.registerBean(registry);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
