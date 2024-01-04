package org.bf.framework.boot.config.life;

import org.bf.framework.boot.util.LogUtil;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

public interface Listener  {
    interface EnvironmentPrepared  extends ApplicationListener<ApplicationEnvironmentPreparedEvent> {
        @Override
        default void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
            LogUtil.currentMethod();
        }
    }
    interface ContextInitialized  extends ApplicationListener<ApplicationContextInitializedEvent> {
        @Override
        default void onApplicationEvent(ApplicationContextInitializedEvent event) {
            LogUtil.currentMethod();
        }
    }
}
