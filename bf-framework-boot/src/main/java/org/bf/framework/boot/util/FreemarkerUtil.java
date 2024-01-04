package org.bf.framework.boot.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.util.Map;

@Slf4j
public class FreemarkerUtil {

    private Configuration config;
    /**
     * Spring官方提供的集成邮件服务的实现类
     */
    public FreemarkerUtil(Configuration cfg){
        this.config = cfg;
    }
    /**
     */
    public String renderTemplate(String template, Map<String, Object> model) {
        try {
            // 获得模板
            Template template1 = config.getTemplate(template);
            // 传入数据模型到模板，替代模板中的占位符，并将模板转化为html字符串
            StringWriter result = new StringWriter(1024);
            template1.process(model, result);
            return result.toString();
        } catch (Exception e) {
            log.error("freemarker template error", e);
            return null;
        }
    }
}
