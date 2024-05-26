package org.bf.framework.common.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class XmlUtils {
    /**
     * -----------xml相关----------------
     */
    public static String beanToXml(Object bean) {
        return beanToXml(bean, CharsetUtil.CHARSET_UTF_8, true);
    }

    /**
     * JavaBean转换成xml
     *
     * @param bean    Bean对象
     * @param charset 编码 eg: utf-8
     * @param format  是否格式化输出eg: true
     * @return 输出的XML字符串
     */
    public static String beanToXml(Object bean, Charset charset, boolean format) {
        StringWriter writer;
        try {
            JAXBContext context = JAXBContext.newInstance(bean.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, charset.name());
            writer = new StringWriter();
            marshaller.marshal(bean, writer);
        } catch (Exception e) {
            throw new RuntimeException("convertToXml 错误：" + e.getMessage(), e);
        }
        return writer.toString();
    }

    public static <T> T xmlToBean(String xml, Class<T> c) {
        return xmlToBean(new StringReader(xml), c);
    }

    @SuppressWarnings("unchecked")
    public static <T> T xmlToBean(Reader reader, Class<T> c) {
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(reader);
        } catch (Exception e) {
            throw new RuntimeException("convertToJava2 错误：" + e.getMessage(), e);
        } finally {
            IoUtil.close(reader);
        }
    }
}

