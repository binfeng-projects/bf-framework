package org.bf.framework.autoconfigure.pay.model;

public class RefundResponse extends BasePayModel {
    /**
     * 本系统的退款id,需全局唯一，同一个订单可以产生多笔退款，直到退完
     */
    private String refundId;
    /**
     * 三方支付平台的退款号
     */
    private String payPlatformRefundId;

    /**
     * 三方支付平台的订单号
     */
    private String payPlatformOrderId;
    /**
     * 三方支付平台的用户id，收到退款的账户
     */
    private String payPlatformUserId;
    /**
     * 状态
     */
    private String status;
    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }
    public String getPayPlatformRefundId() {
        return payPlatformRefundId;
    }

    public void setPayPlatformRefundId(String payPlatformRefundId) {
        this.payPlatformRefundId = payPlatformRefundId;
    }

    public String getPayPlatformOrderId() {
        return payPlatformOrderId;
    }

    public void setPayPlatformOrderId(String payPlatformOrderId) {
        this.payPlatformOrderId = payPlatformOrderId;
    }

    public String getPayPlatformUserId() {
        return payPlatformUserId;
    }

    public void setPayPlatformUserId(String payPlatformUserId) {
        this.payPlatformUserId = payPlatformUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
