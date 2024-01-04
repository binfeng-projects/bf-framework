package org.bf.framework.boot.annotation.auto;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ConfigImportRegistrar.class)
public @interface EnableConfig {
  /**
   * 配置处理类
   */
  @AliasFor("value")
  Class<? extends EnableConfigHandler> clazz() default EnableConfigHandler.class;
  /**
   * 配置处理类
   */
  @AliasFor("clazz")
  Class<? extends EnableConfigHandler> value() default EnableConfigHandler.class;
}
