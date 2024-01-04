package org.bf.framework.boot.config.life;

import org.bf.framework.boot.util.LogUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * bean生命周期相关
 */
public interface BeanLifeCycle extends InstantiationAwareBeanPostProcessor,MergedBeanDefinitionPostProcessor,InitializingBean {
    interface BeanPost extends BeanPostProcessor {
        @Override
        default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            LogUtil.currentMethod();
            return bean;
        }

        @Override
        default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            LogUtil.currentMethod();
            return bean;
        }
    }
    @Override
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        LogUtil.currentMethod();
        return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInstantiation(beanClass, beanName);
    }
    @Override
    default void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName){
        LogUtil.currentMethod();
    }
    @Override
    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        LogUtil.currentMethod();
        return true;
    }
    @Override
    default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        LogUtil.currentMethod();
        return pvs;
    }
    @Override
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LogUtil.currentMethod();
        return bean;
    }

    @Override
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        LogUtil.currentMethod();
        return bean;
    }

    @Override
    default void resetBeanDefinition(String beanName) {
        LogUtil.currentMethod();
    }

    @Override
    default void afterPropertiesSet() throws Exception {
        LogUtil.currentMethod();
    }
}
