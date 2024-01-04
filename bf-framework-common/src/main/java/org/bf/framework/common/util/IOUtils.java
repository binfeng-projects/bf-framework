package org.bf.framework.common.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    public static void closeQuietly(Closeable c){
        IoUtil.close(c);
    }

    public static void closeQuietly(Closeable ... cs) {
        if(ArrayUtil.isEmpty(cs)){
            return;
        }
        for (Closeable c : cs) {
            IoUtil.close(c);
        }
    }

    public static void copy(InputStream in, OutputStream out){
        IoUtil.copy(in,out);
    }
    public static byte[] toByteArray(InputStream in){
        return IoUtil.readBytes(in);
    }
}
