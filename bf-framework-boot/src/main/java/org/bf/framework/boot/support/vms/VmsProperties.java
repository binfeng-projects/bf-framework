package org.bf.framework.boot.support.vms;

public class VmsProperties {
    /**
     * 云平台,可选值 tencent,aliyun,aws
     */
    private String platform;
    /**
     * 账户
     */
    private String accessKeyId;
    /**
     * 密码
     */
    private String accessKeySecret;
    /**
     * 所属区域
     */
    private String region;
    /**
     * 模版Id
     */
    private String templateId;
    /**
     * 展示号码,阿里云支持（隐藏真实号码）
     */
    private String callShowNumber;
    /**
     * 腾讯云需要
     */
    private String appId;
    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getCallShowNumber() {
        return callShowNumber;
    }

    public void setCallShowNumber(String callShowNumber) {
        this.callShowNumber = callShowNumber;
    }
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }

}
