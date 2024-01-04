package org.bf.framework.boot.base;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermCheck {
    /**
     * 需要的权限值，默认0，一般为此模块最小权限，比如读取查找等操作
     */
    int perm() default 0;

    /**
     * 资源
     */
    String resource() default "";
}
