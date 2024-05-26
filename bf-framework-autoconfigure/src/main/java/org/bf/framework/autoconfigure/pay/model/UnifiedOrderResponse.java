package org.bf.framework.autoconfigure.pay.model;

public class UnifiedOrderResponse extends BasePayModel {

    /** 支付数据包类型 参考 {@link org.bf.framework.autoconfigure.pay.PayProxy.PayDataTypeEnum} **/
    private String payDataType;
    /**
     * 预下单返回的结果，一般作为真正支付发起的参数
     */
    private String payResult;
    /** 支付方式  jsapi,wap,h5等 参考 {@link org.bf.framework.autoconfigure.pay.WechatPayProxy.WechatTradeTypeEnum}   **/
    private String tradeType;

    public String getPayDataType() {
        return payDataType;
    }

    public void setPayDataType(String payDataType) {
        this.payDataType = payDataType;
    }

    public String getPayResult() {
        return payResult;
    }

    public void setPayResult(String payResult) {
        this.payResult = payResult;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
}
