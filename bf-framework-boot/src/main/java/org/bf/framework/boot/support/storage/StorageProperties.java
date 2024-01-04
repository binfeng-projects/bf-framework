package org.bf.framework.boot.support.storage;

public class StorageProperties {
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
     * 桶名
     */
    private String bucketName;

    /**
     * 阿里云特有
     */
    private String internalEndpoint;

    public String getInternalEndpoint() {
        return internalEndpoint;
    }

    public void setInternalEndpoint(String internalEndpoint) {
        this.internalEndpoint = internalEndpoint;
    }

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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

}
