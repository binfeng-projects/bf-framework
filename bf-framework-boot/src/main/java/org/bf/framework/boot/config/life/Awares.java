package org.bf.framework.boot.config.life;

import org.bf.framework.boot.util.LogUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.util.StringValueResolver;

/**
 * aware相关的扩展点
 */
public interface Awares {
    /**
     * 所有aware接口
     */
    interface All extends BeanFactory, AllContext {
    }

    /**
     * 常用的两个
     */
    interface EnvAPP extends EnvironmentAware, ApplicationContextAware {
        default void setEnvironment(Environment environment){
            LogUtil.currentMethod();
        }
        default void setApplicationContext(ApplicationContext applicationContext) throws BeansException{
            LogUtil.currentMethod();
        }
    }

    /**
     * bean工厂相关
     */
    interface BeanFactory extends BeanFactoryAware, BeanNameAware, BeanClassLoaderAware {
        default void setBeanFactory(org.springframework.beans.factory.BeanFactory beanFactory) throws BeansException {
            LogUtil.currentMethod();
        }
       default void setBeanName(String name){
           LogUtil.currentMethod();
       }

       default void setBeanClassLoader(ClassLoader classLoader){
           LogUtil.currentMethod();
       }
    }

    /**
     * 所有上下文相关
     */
    interface AllContext extends EnvironmentAware, AppCtx, ResourceContex {
        default void setEnvironment(Environment environment){
            LogUtil.currentMethod();
        }
    }

    /**
     * App上下文
     */
    interface AppCtx extends ApplicationStartupAware, ApplicationContextAware, ApplicationEventPublisherAware {
        default void setApplicationContext(ApplicationContext applicationContext) throws BeansException{
            LogUtil.currentMethod();
        }
        default void setApplicationStartup(ApplicationStartup applicationStartup){
            LogUtil.currentMethod();
        }
        default void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher){
            LogUtil.currentMethod();
        }
    }

    /**
     * resource上下文
     */
    interface ResourceContex extends EmbeddedValueResolverAware, MessageSourceAware, ResourceLoaderAware {
        default void setEmbeddedValueResolver(StringValueResolver resolver){
            LogUtil.currentMethod();
        }
        default void setMessageSource(MessageSource messageSource){
            LogUtil.currentMethod();
        }
        default void setResourceLoader(ResourceLoader resourceLoader){
            LogUtil.currentMethod();
        }
    }
}
