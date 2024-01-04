package org.bf.framework.common.util;

import cn.hutool.core.util.JdkUtil;

public class SystemUtil {

    static String OS_NAME = "os.name";
    static String OS_VERSION = "os.version";
    static String OS_ARCH = "os.arch";
    static String BOOT_VERSION = "spring-boot.formatted-version";

    /**
     * 操作系统
     */
    public static String osName() {
        return System.getProperty(OS_NAME);
    }
    /**
     * 操作系统版本
     */
    public static String osVersion() {
        return System.getProperty(OS_VERSION);
    }
    /**
     * 操作系统架构
     */
    public static String osArch() {
        return System.getProperty(OS_ARCH);
    }
    public static int jdkVersion() {
        return JdkUtil.JVM_VERSION;
    }
    public static boolean isJdk8() {
        return JdkUtil.IS_JDK8;
    }
    /**
     * spring-boot版本
     * @return
     */
    public static String bootVersion() {
        return System.getProperty(BOOT_VERSION);
    }
}
