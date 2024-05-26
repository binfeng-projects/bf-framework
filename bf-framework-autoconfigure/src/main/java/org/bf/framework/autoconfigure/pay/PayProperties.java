package org.bf.framework.autoconfigure.pay;

import java.util.Map;

public class PayProperties {
    public String getSiteDomain() {
        return siteDomain;
    }

    public void setSiteDomain(String siteDomain) {
        this.siteDomain = siteDomain;
    }

    /**
     * 支付平台
     */
    private String payPlatform;
    /**
     * 子商户ID ,如果没有子商户相关信息，说明是普通商户（非服务商模式的特约商户）
     */
    private String subAppId;
    /**
     * 微信为subMchId,支付宝为子商户appAuthToken,云闪付为子商户编号merId
     */
    private String subMchInfo;
    /**
     * 应用App ID
     */
    private String appId;

    /**
     * 应用AppSecret
     */
    private String appSecret;
    /**
     * 微信支付商户号,支付宝为pid
     */
    private String mchId;
    /**
     * 支付宝，微信为应用私钥，PayPal为secretId
     */
    private String privateKey;
    /**
     * 是否沙盒环境
     */
    private boolean sandbox =  true;
    /**
     * 站点域名，回调等需要
     */
    private String siteDomain;
    /**
     * 各个支付厂家的个性化配置
     * 微信：
     *      serialNo: #商户证书序列号
     *      apiKey: #API V3密钥
     * 支付宝:
     * #                charset: UTF-8
     * #                signType: RSA2
     * #                format: JSON
     *                 aliPayPublicKey: # 支付宝公钥
     *                 certModel: # 是否使用证书方式 true false
     *                 appPublicCert: #app证书
     *                 alipayPublicCert: #支付宝公钥证书（.crt格式）
     *                 alipayRootCert: #支付宝根证书
     */
    private Map<String,String> config;

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getPayPlatform() {
        return payPlatform;
    }

    public void setPayPlatform(String payPlatform) {
        this.payPlatform = payPlatform;
    }

    public String getSubAppId() {
        return subAppId;
    }

    public void setSubAppId(String subAppId) {
        this.subAppId = subAppId;
    }

    public String getSubMchInfo() {
        return subMchInfo;
    }

    public void setSubMchInfo(String subMchInfo) {
        this.subMchInfo = subMchInfo;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public String getConfigByKey(String key) {
        return config.get(key);
    }
}
