package org.bf.framework.common.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.*;

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
    public static void writeFile(String data,File file){
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeFile(String data,String parent,String child){
        try {
            File parentFile = new File(parent);
            parentFile.mkdirs();
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(parentFile,child)));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
