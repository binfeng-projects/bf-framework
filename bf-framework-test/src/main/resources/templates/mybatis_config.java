package ${currentPackage};
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import ${corePackage}.proxy.MiddlewareHolder;
/**
 * MybatisConfiguration
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class MybatisConfiguration {

[#list schemas as schema]
    @Configuration(proxyBeanMethods = false)
    @MapperScan(basePackages = "${corePackage}.db.${schema.sqlName}",sqlSessionFactoryRef = MiddlewareHolder.BEAN${middlewareBean?upper_case}_${schema.sqlName?upper_case})
    static class ${schema.sqlName}_Config{
    }
[/#list]
}

