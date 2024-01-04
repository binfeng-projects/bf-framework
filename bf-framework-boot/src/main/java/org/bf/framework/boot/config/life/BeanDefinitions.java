package org.bf.framework.boot.config.life;

import org.bf.framework.boot.util.LogUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public interface BeanDefinitions extends BeanDefinitionRegistryPostProcessor{
    @Override
    default void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException{
        LogUtil.currentMethod();
    }

    @Override
    default void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException{
        LogUtil.currentMethod();
    }
}
