package org.bf.framework.boot.support;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;

import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.*;

@Data
@Accessors(chain = true)
public class Middleware {
    private static final Map<String, List<Middleware>> ALL_MIDDLEWARE = MapUtils.newConcurrentHashMap();
    private static final Map<Class<?>, Middleware> PRIMARY_MIDDLEWARE = MapUtils.newConcurrentHashMap();
    public static List<Middleware> register(String prefixAndSchema, List<Middleware> middlewareList) {
        if(CollectionUtils.isEmpty(middlewareList)) {
            return middlewareList;
        }
        for (Middleware m : middlewareList) {
            if(m.getType() == null || m.getBean() == null) {
                continue;
            }
            //bean的名称是有一定规范的，外界可按命名规范注入bean
            String beanName = m.getPrefix() + DOT + m.getSchemaName() + "_" + m.getType().getSimpleName();
            SpringUtil.registrySingleton(beanName,m.getBean());
        }
        List<Middleware> result = ALL_MIDDLEWARE.getOrDefault(prefixAndSchema,CollectionUtils.newArrayList());
        ALL_MIDDLEWARE.put(prefixAndSchema,result);
        result.addAll(middlewareList);
        return result;
    }

    public static Map<Class<?>, Middleware> registerPrimary(List<Middleware> middlewareList) {
        if(CollectionUtils.isEmpty(middlewareList)) {
            return PRIMARY_MIDDLEWARE;
        }
        for (Middleware m : middlewareList) {
            PRIMARY_MIDDLEWARE.putIfAbsent(m.type,m);
            if(m.getType() == null || m.getBean() == null) {
                continue;
            }
            //bean的名称是有一定规范的，外界可按命名规范注入bean
            String beanName = PRIMARY + m.getType().getSimpleName();
            //注册DataSource到spring
            SpringUtil.registrySingleton(beanName,m.getBean());
        }
        return PRIMARY_MIDDLEWARE;
    }
    public static Map<String, List<Middleware>> getByPrefix(String prefix) {
        Map<String,List<Middleware>> result = MapUtils.newHashMap();
        if(StringUtils.isBlank(prefix)) {
            return result;
        }
        for (Map.Entry<String, List<Middleware>> entry : ALL_MIDDLEWARE.entrySet()) {
            Middleware middleware = entry.getValue().get(0);
            if(middleware.getPrefix().equals(prefix)) {
                result.put(middleware.getSchemaName(),entry.getValue());
            }
        }
        return result;
    }
    public static List<Middleware> getByPrefixAndSchema(String prefix, String schemaName) {
        Map<String,List<Middleware>> result = getByPrefix(prefix);
        if(MapUtils.isEmpty(result)) {
            return null;
        }
        if(result.size() == 1) {
            return result.entrySet().stream().findFirst().get().getValue();
        }
        return result.get(schemaName);
    }
    public static Object getMiddlewareBean(String prefix,String schemaName,Class<?> type) {
        List<Middleware> result = getByPrefixAndSchema(prefix,schemaName);
        if(CollectionUtils.isEmpty(result) || type == null) {
            return null;
        }
        for (Middleware m : result) {
            if(m.getType() == null || m.getBean() == null) {
                continue;
            }
            if(type.isAssignableFrom(m.getType())) {
                return m.getBean();
            }
        }
        return null;
    }
    private String prefix;
    private String schemaName;
    private Class<?> type;
    private Object bean;
    //默认，bean和schema全生成。false只生成schema,不生成bean。null全都不生成
    private Boolean codeGen = Boolean.TRUE;
}
