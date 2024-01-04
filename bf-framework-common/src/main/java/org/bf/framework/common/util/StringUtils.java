package org.bf.framework.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * Map转paramsString return name=value&name=value
     */
    public static StringBuilder getQueryString(String url, Map<String, Object> p) {
        StringBuilder sb = new StringBuilder();
        if (isBlank(url)) {
            return sb;
        }
        sb.append(url);
        if (MapUtils.isEmpty(p)) {
            return sb;
        }
        boolean firstFlag = true;
        for (Map.Entry<String, Object> entry : p.entrySet()) {
            if (firstFlag) {
                sb.append("?");
                firstFlag = false;
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb;
    }


    public static boolean isBlank(CharSequence s){
        return StrUtil.isBlank(s);
    }

    public static boolean isNotBlank(CharSequence s){
        return !isBlank(s);
    }

    public static boolean endsWithIgnoreCase(CharSequence str, CharSequence suffix){
        return StrUtil.endWithIgnoreCase(str,suffix);
    }

    public static boolean allBlank(String... args) {
        return StrUtil.isAllBlank(args);
    }

    public static boolean hasBlank(String... args) {
        return StrUtil.hasBlank(args);
    }

    public static String join(CharSequence conjunction, Object... objs) {
        return StrUtil.join(conjunction, objs);
    }

    public static <T> String join(CharSequence conjunction, Iterable<T> iterable) {
        return CollUtil.join(iterable, conjunction);
    }

}
