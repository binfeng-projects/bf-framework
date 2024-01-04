package org.bf.framework.boot.config.life;

/**
 * 包含基本的spring扩展点
 */
public interface LifeCycleAll extends Awares.All, BeanDefinitions, BeanLifeCycle, Listener.EnvironmentPrepared {
}
