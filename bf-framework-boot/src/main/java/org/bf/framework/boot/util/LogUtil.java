package org.bf.framework.boot.util;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class LogUtil {
    public static void currentMethod(){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement trace = null;
        if (stackTrace.length >= 3) { // Adjust this based on your needs
            trace= stackTrace[2];
        }
        if(trace != null && trace.getClassName().startsWith("org.bf.framework")){
            log.info(trace.getClassName() + " : " + trace.getMethodName());
        }
    }
}
