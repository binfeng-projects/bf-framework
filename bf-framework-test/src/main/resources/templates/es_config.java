package ${currentPackage};
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import ${corePackage}.proxy.MiddlewareHolder;
/**
 * esConfig
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class ElasticsearchConfiguration {

[#list schemas as schema]
    @Configuration(proxyBeanMethods = false)
    @EnableElasticsearchRepositories(basePackages = "${corePackage}.${middlewareType}.${schema.sqlName}.repository",elasticsearchTemplateRef = MiddlewareHolder.BEAN${middlewareBean?upper_case}_${schema.sqlName?upper_case})
    static class ${schema.sqlName}_Config{
    }
[/#list]
}

