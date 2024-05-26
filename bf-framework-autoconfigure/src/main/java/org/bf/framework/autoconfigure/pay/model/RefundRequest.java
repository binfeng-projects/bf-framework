package org.bf.framework.autoconfigure.pay.model;

import java.util.HashMap;
import java.util.Map;

public class RefundRequest extends BasePayModel {
    /**
     * 本系统的退款id,需全局唯一，同一个订单可以产生多笔退款，直到退完
     */
    private String refundId;
    /**
     * 三方支付平台的订单号,一般和payOrderId二选一传给三方
     */
    private String payPlatformOrderId;
    /**
     * 退款原因
     */
    private String reason;
    /** 订单金额， 单位：分 **/
    private Long amount;
    /** 退款金额 单位：分*/
    private Long refundAmount;
    /** 货币代码 **/
    private String currency = "CNY";

    /** 客户端IP地址 **/
    private String clientIp;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 扩展参数 **/
    private Map<String,Object> ext = new HashMap<>();

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
    public Object getExtValue(String key) {
        return ext.get(key);
    }
    public void setExtValue(String key,Object v) {
        this.ext.put(key,v);
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public String getPayPlatformOrderId() {
        return payPlatformOrderId;
    }

    public void setPayPlatformOrderId(String payPlatformOrderId) {
        this.payPlatformOrderId = payPlatformOrderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

}
