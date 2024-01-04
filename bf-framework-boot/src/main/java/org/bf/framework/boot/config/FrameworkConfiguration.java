package org.bf.framework.boot.config;

import org.bf.framework.boot.support.cache.CacheConfig;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

@Configuration(proxyBeanMethods = false)
@Import(FrameworkConfiguration.ConfigurationImportSelector.class)
public class FrameworkConfiguration {
    static class ConfigurationImportSelector implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata metadata) {
            return new String[]{CacheConfig.class.getName()};
        }

    }

}
