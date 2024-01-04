package org.bf.framework.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MapUtils {
    public static boolean isEmpty(Map<?,?> map){
        return MapUtil.isEmpty(map);
    }
    public static boolean isNotEmpty(Map<?,?> map){
        return !isEmpty(map);
    }
    public static <K, V> Map<K, V> mergeMaps(Map<K, V> targetMap, Map<K, V> sourceMap, boolean override) {
        if(isEmpty(sourceMap)) {
            return targetMap;
        }
        if(isEmpty(targetMap)) {
            return sourceMap;
        }
        if(override) {
            targetMap.putAll(sourceMap);
            return targetMap;
        }
        for (Map.Entry<K, V> entry : sourceMap.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            targetMap.putIfAbsent(key, value);
        }
        return targetMap;
    }
    public static <K , V > HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    public static <K , V > HashMap<K, V> newHashMap(Map<K, V> input) {
        HashMap<K, V> result = new HashMap<K, V>();
        if(isNotEmpty(input)) {
            result.putAll(input);
        }
        return result;
    }
    public static <K, V> HashMap<K, V> newHashMap(int size, boolean isLinked) {
        int initialCapacity = (int)((float)size / 0.75F) + 1;
        return (HashMap)(isLinked ? new LinkedHashMap(initialCapacity) : new HashMap(initialCapacity));
    }

    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return newHashMap(size, false);
    }

    public static <K, V> HashMap<K, V> newHashMap(boolean isLinked) {
        return newHashMap(16, isLinked);
    }

    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap(comparator);
    }

    public static <K, V> TreeMap<K, V> newTreeMap(Map<K, V> map, Comparator<? super K> comparator) {
        TreeMap<K, V> treeMap = new TreeMap(comparator);
        if (!isEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap(16);
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int size) {
        int initCapacity = size <= 0 ? 16 : size;
        return new ConcurrentHashMap(initCapacity);
    }

    public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(Map<K, V> map) {
        return isEmpty(map) ? new ConcurrentHashMap(16) : new ConcurrentHashMap(map);
    }

    public static <K, V> HashMap<K, V> of(K key, V value) {
        return of(key, value, false);
    }

    public static <K, V> HashMap<K, V> of(K key, V value, boolean isOrder) {
        HashMap<K, V> map = newHashMap(isOrder);
        map.put(key, value);
        return map;
    }

    public static <K, V> Map<K, List<V>> toListMap(Iterable<? extends Map<K, V>> mapList) {
        return MapUtil.toListMap(mapList);
    }

    public static <K, V> Map<K, List<V>> grouping(Iterable<Map.Entry<K, V>> entries) {
        return MapUtil.grouping(entries);
    }

    public static <K, V> Map<K, V> filter(Map<K, V> map, K... keys) {
        return MapUtil.filter(map,keys);
    }

    public static String getStr(Map<?, ?> map, Object key, String defaultValue) {
        return MapUtil.getStr(map, key,defaultValue);
    }

    public static Integer getInt(Map<?, ?> map, Object key, Integer defaultValue) {
        return MapUtil.getInt(map, key,defaultValue);
    }

    public static Double getDouble(Map<?, ?> map, Object key, Double defaultValue) {
        return MapUtil.getDouble(map, key,defaultValue);
    }

    public static Float getFloat(Map<?, ?> map, Object key, Float defaultValue) {
        return MapUtil.getFloat(map, key,defaultValue);
    }

    public static Short getShort(Map<?, ?> map, Object key, Short defaultValue) {
        return MapUtil.getShort(map, key,defaultValue);
    }


    public static Boolean getBool(Map<?, ?> map, Object key, Boolean defaultValue) {
        return MapUtil.getBool(map, key,defaultValue);
    }

    public static Character getChar(Map<?, ?> map, Object key, Character defaultValue) {
        return MapUtil.getChar(map, key,defaultValue);
    }

    public static Long getLong(Map<?, ?> map, Object key, Long defaultValue) {
        return MapUtil.getLong(map, key,defaultValue);
    }

    public static Date getDate(Map<?, ?> map, Object key, Date defaultValue) {
        return MapUtil.getDate(map, key,defaultValue);
    }

    public static <K, V> Map<K, V> removeNullValue(Map<K, V> map) {
        if (isEmpty(map)) {
            return map;
        } else {
            Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();

            while(iter.hasNext()) {
                Map.Entry<K, V> entry = (Map.Entry)iter.next();
                if (null == entry.getValue()) {
                    iter.remove();
                }
            }

            return map;
        }
    }

    public static void clear(Map<?, ?>... maps) {
        Map[] var1 = maps;
        int var2 = maps.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Map<?, ?> map = var1[var3];
            if (isNotEmpty(map)) {
                map.clear();
            }
        }

    }
    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> mappingFunction) {
        return MapUtil.computeIfAbsent(map,key,mappingFunction);
    }

    public static Map<String,Object> beanToMap(Object bean) {
        return BeanUtil.beanToMap(bean);
    }

    public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isToCamelCase, boolean isIgnoreError) {
        return BeanUtil.fillBeanWithMap(map, bean, isToCamelCase, isIgnoreError);
    }
}
