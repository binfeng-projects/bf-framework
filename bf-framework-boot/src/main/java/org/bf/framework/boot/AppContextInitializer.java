package org.bf.framework.boot;

import org.bf.framework.boot.config.SystemHelp;
import org.bf.framework.boot.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(-100)
public class AppContextInitializer implements EnvironmentPostProcessor,
        ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        //初始化系统环境
        SpringUtil.initFrameworkEnv(environment);
        //注册一些需要放到Environment中的信息,提供spi扩展
        SystemHelp.registerEnvironmentProperty(environment);
    }
    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        BeanRegisterUtil.registrySingleton(ctx.getBeanFactory(),this);
        //初始化springUtil，后续供业务使用
        SpringUtil.setApplicationContext(ctx);
        //注册一些需要放到SystemProperty中的信息
        SystemHelp.registerSystemProperty(ctx.getEnvironment());
    }
//    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry r) throws BeansException {
//        BeanRegisterUtil.registry(r, FrameworkConfiguration.class, BeanDefinition.ROLE_INFRASTRUCTURE);
//    }
//
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//
//    }
}
