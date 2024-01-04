package org.bf.framework.boot.config;

import org.bf.framework.boot.constant.FrameworkConst;
import org.bf.framework.boot.util.SpringInjector;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.List;

/**
 * spring默认好像没有yaml格式的解析工厂,但是有解析类。
 * 解析文件名，适配各种文件资源加载。当前支持yaml和默认的property格式
 *     可以使用@PropertySource(value = {"classpath:hello.yaml"},factory = CommonFileSourceFactory.class)
 */
public class CommonFileSourceFactory extends DefaultPropertySourceFactory {
    private static final YamlPropertySourceLoader yamlLoader = SpringInjector.getInstance(YamlPropertySourceLoader.class);
    @Override
    public PropertySource<?>  createPropertySource(String name, EncodedResource resource) throws IOException {
        Resource resourceResource = resource.getResource();
        if (!resourceResource.exists()) {
            return null;
        }
        String fileName = resourceResource.getFilename();
        if (fileName.endsWith(FrameworkConst.YML_FILE_EXTENSION) || fileName.endsWith(FrameworkConst.YAML_FILE_EXTENSION)) {
            List<PropertySource<?>> sources = yamlLoader.load(resourceResource.getFilename(), resourceResource);
            return sources.get(0);
        }
        //默认认为是property
        return super.createPropertySource(name, resource);
    }
}
