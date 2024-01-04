package org.bf.framework.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import jakarta.xml.bind.JAXBContext;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSON {
    /**
     * 照顾下fastjson用户
     */
    public static String toJSONString(Object obj) {
        if(obj == null){
            return null;
        }
        if(obj.getClass().isPrimitive() || obj instanceof Number){
            return obj.toString();
        }
        return GSON.toJson(obj);
    }

    /**
     * 照顾下fastjson用户
     */
    public static <T> T parseObject(String str, Class<T> c) {
        if(c == null || StringUtils.isBlank(str)){
            return null;
        }
        return GSON.fromJson(str,c);
    }

        /** -----------xml相关----------------
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

    public static Gson getInstance() {
        return GSON;
    }
    public static Type MAP_STR_OBJ_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MAP_STR_OBJ_TYPE, new MapDeserializerDoubleAsIntFix())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    /**
     * To json string.
     *
     * @param bean the bean
     * @return the string
     */
    public static String toJson(Object bean) {
        return GSON.toJson(bean);
    }

    /**
     * To json string.
     *
     * @param builder the builder
     * @param bean    the bean
     * @return the string
     */
    public static String toJson(GsonBuilder builder, Object bean) {
        return builder.create().toJson(bean);
    }

    /**
     * Parse t.
     *
     * @param <T>  the type parameter
     * @param json the json
     * @param clz  the clz
     * @return the t
     */
    public static <T> T parse(String json, Class<T> clz) {
        return GSON.fromJson(json, clz);
    }

    /**
     * Parse t.
     *
     * @param <T>     the type parameter
     * @param builder the builder
     * @param json    the json
     * @param clz     the clz
     * @return the t
     */
    public static <T> T parse(GsonBuilder builder, String json, Class<T> clz) {
        return builder.create().fromJson(json, clz);
    }

    /**
     * Parse t.
     *
     * @param <T>  the type parameter
     * @param json the json
     * @param type the type
     * @return the t
     */
    public static <T> T parse(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    public static <T> T parse(GsonBuilder builder, String json, Type type) {
        return builder.create().fromJson(json, type);
    }

    /**
     * To json bytes byte [ ].
     *
     * @param value the value
     * @return the byte [ ]
     */
    public static byte[] toJsonBytes(Object value) {
        return toJson(value).getBytes(StandardCharsets.UTF_8);
    }

    public final static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        @Override
        public LocalDate deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            String timestamp = element.getAsJsonPrimitive().getAsString();
            return LocalDate.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
    public final static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        @Override
        public LocalDateTime deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            String timestamp = element.getAsJsonPrimitive().getAsString();
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    public final static class MapDeserializerDoubleAsIntFix implements JsonDeserializer<Map<String, Object>> {

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, Object> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            return (Map<String, Object>) read(element);
        }

        private Object read(JsonElement in) {
            if (in.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                JsonArray arr = in.getAsJsonArray();
                for (JsonElement anArr : arr) {
                    list.add(read(anArr));
                }
                return list;
            } else if (in.isJsonObject()) {
                Map<String, Object> map = new LinkedTreeMap<>();
                JsonObject obj = in.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
                for (Map.Entry<String, JsonElement> entry : entitySet) {
                    map.put(entry.getKey(), read(entry.getValue()));
                }
                return map;
            } else if (in.isJsonPrimitive()) {
                JsonPrimitive prim = in.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    return prim.getAsBoolean();
                } else if (prim.isString()) {
                    return prim.getAsString();
                } else if (prim.isNumber()) {
                    Number num = prim.getAsNumber();
                    // here you can handle double int/long values
                    // and return any type you want
                    // this solution will transform 3.0 float to long values
                    if (Math.ceil(num.doubleValue()) == num.longValue())
                        return num.longValue();
                    else {
                        return num.doubleValue();
                    }
                }
            }
            return null;
        }
    }
}
