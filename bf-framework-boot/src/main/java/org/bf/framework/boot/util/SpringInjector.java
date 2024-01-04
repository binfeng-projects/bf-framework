package org.bf.framework.boot.util;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.bf.framework.boot.config.CommonFileSourceFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;

public class SpringInjector {
    private static volatile Injector s_injector;
    private static final Object lock = new Object();

    private static Injector getInjector() {
        if (s_injector == null) {
            synchronized (lock) {
                if (s_injector == null) {
                    s_injector = Guice.createInjector(new SpringModule());
                }
            }
        }

        return s_injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return getInjector().getInstance(clazz);
        } catch (Throwable ex) {
            throw new RuntimeException(String.format("Unable to load instance for %s!", clazz.getName()), ex);
        }
    }

    private static class SpringModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(YamlPropertySourceLoader.class).in(Singleton.class);
            bind(CommonFileSourceFactory.class).in(Singleton.class);
        }
    }

}
