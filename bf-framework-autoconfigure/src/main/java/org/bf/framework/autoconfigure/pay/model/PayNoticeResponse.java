package org.bf.framework.autoconfigure.pay.model;

import java.util.Map;

public class PayNoticeResponse extends BasePayModel {
    /**
     * 通知类型 参考 {@link org.bf.framework.autoconfigure.pay.PayProxy.NoticeTypeEnum}
     **/
    private String noticeType;
    /**
     * 三方支付平台的订单号
     */
    private String payPlatformOrderId;
    /**
     * 三方支付平台的用户id，例如微信的openid
     */
    private String payPlatformUserId;
    /**
     * 三方通知的状态
     */
    private String tradeState;
    /**
     * 返回给三方的响应体
     */
    private Map<String,Object> responseBody;
    public Map<String, Object> getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Map<String, Object> responseBody) {
        this.responseBody = responseBody;
    }
    public String getPayPlatformUserId() {
        return payPlatformUserId;
    }

    public void setPayPlatformUserId(String payPlatformUserId) {
        this.payPlatformUserId = payPlatformUserId;
    }

    public String getNoticeType() {
        return noticeType;
    }
    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }
    public String getPayPlatformOrderId() {
        return payPlatformOrderId;
    }
    public void setPayPlatformOrderId(String payPlatformOrderId) {
        this.payPlatformOrderId = payPlatformOrderId;
    }
    public String getTradeState() {
        return tradeState;
    }
    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

}
