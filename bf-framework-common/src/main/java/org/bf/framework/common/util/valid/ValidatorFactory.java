package org.bf.framework.common.util.valid;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.common.util.CollectionUtils;
import org.hibernate.validator.HibernateValidator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;

/**
 * 可以定义一个 继承这个类，那么就会覆盖默认的Validator获取
 */
@Slf4j
public abstract class ValidatorFactory {
    static Validator v;

    public abstract Validator getValidator();

    public static Validator getInstance() {
        if (null != v) {
            return v;
        }
        try {
            Set<Class<?>> cls = ClassUtil.scanPackageBySuper(null, ValidatorFactory.class);
            if (CollectionUtils.isEmpty(cls)) {
                throw new RuntimeException("use default validator");
            }
            Class<?> c = cls.iterator().next();
            ValidatorFactory holder = (ValidatorFactory) ReflectUtil.newInstance(c);
            v = holder.getValidator();
        } catch (Exception e) {
            //如果没有叫ValidHolderImpl且继承本类的类，用默认自定义的
            log.error("please assign a class extends ValidHolder", e);
            v = Validation.byProvider(HibernateValidator.class).configure().
                    failFast(false).buildValidatorFactory().getValidator();
        }
        return v;
    }
}
