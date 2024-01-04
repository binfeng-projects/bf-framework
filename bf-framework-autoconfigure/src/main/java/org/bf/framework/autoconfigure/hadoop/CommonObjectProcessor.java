package org.bf.framework.autoconfigure.hadoop;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.util.SpringUtil;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.hadoop.config.common.annotation.ObjectPostProcessor;

@Slf4j
public class CommonObjectProcessor implements ObjectPostProcessor<Object> {
    @Override
    public Object postProcess(Object object) {
        if(object instanceof BeanFactoryAware) {
            ((BeanFactoryAware)object).setBeanFactory(SpringUtil.getContext());
        }
        if(object instanceof InitializingBean) {
            try {
                ((InitializingBean)object).afterPropertiesSet();
            } catch (Exception e) {
                log.error("afterPropertiesSet error",e);
                throw new RuntimeException();
            }
        }
        return object;
    }
}
