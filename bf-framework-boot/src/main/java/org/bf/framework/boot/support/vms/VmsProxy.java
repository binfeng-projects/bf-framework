package org.bf.framework.boot.support.vms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bf.framework.common.util.CollectionUtils;

import java.util.List;
import java.util.Set;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@Slf4j
public abstract class VmsProxy {
    protected String endpoint;

    protected VmsProperties config;

    public VmsProxy(VmsProperties cfg) {
        if (cfg == null) {
            throw new RuntimeException("config empty");
        }
        assert StringUtils.isNotBlank(cfg.getPlatform());
        this.config = cfg;
        if (CLOUD_PLATFORM_ALIYUN.equals(config.getPlatform())) {

        } else if (CLOUD_PLATFORM_AWS.equals(config.getPlatform())) {

        } else if (CLOUD_PLATFORM_TENCENT.equals(config.getPlatform())) {

        } else {
            throw new RuntimeException("unknown cloud platform " + config.getPlatform());
        }
        this.endpoint = formatEndpoint(config);
    }

    public abstract String formatEndpoint(VmsProperties cfg);

    public String callPhone(Set<String> mobiles,String text) {
        return callPhone(mobiles, text,config.getTemplateId());
    }
    public String callPhone(Set<String> mobiles,String text,String templateId){
        if(CollectionUtils.isEmpty(mobiles) || org.bf.framework.common.util.StringUtils.isBlank(templateId) || org.bf.framework.common.util.StringUtils.isBlank(text)) {
            return "param empty";
        }
        List<String> errors = CollectionUtils.newArrayList();
        for (String mobile : mobiles) {
            String errmsg = null;
            try {
                errmsg = callPhone(mobile,text,templateId);
            } catch (Exception e) {
                log.error("call error",e);
                errmsg = e.getMessage();
            }
            if(StringUtils.isNotBlank(errmsg)) {
                errors.add(String.format("mobile %s, content: %s, errmsg %s", mobile,text,errmsg));
            }
        }
        if (CollectionUtils.isNotEmpty(errors)) {
            return StringUtils.join(errors, "\r\n");
        }
        return null;
    }
    public abstract String callPhone(String mobile,String text,String templateId);
}