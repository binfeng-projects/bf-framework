package org.bf.framework.boot.annotation.condition;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(BeanCondition.class)
public @interface OnMissingBean {

	Class<?>[] value() default {};

	String[] type() default {};

	Class<?>[] ignored() default {};

	String[] ignoredType() default {};

	Class<? extends Annotation>[] annotation() default {};

	String[] name() default {};

	SearchStrategy search() default SearchStrategy.ALL;

	Class<?>[] parameterizedContainer() default {};

}
