package org.bf.framework.autoconfigure.pay.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UnifiedOrderRequest extends BasePayModel {
    public static final byte DIVISION_MODE_FORBID = 0; //该笔订单不允许分账
    public static final byte DIVISION_MODE_AUTO = 1; //支付成功按配置自动完成分账
    public static final byte DIVISION_MODE_MANUAL = 2; //商户手动分账(解冻商户金额)

    /** 支付方式  jsapi,wap,h5等 参考 {@link org.bf.framework.autoconfigure.pay.WechatPayProxy.WechatTradeTypeEnum}   **/
    private String tradeType;

    /** 支付数据包类型 参考 {@link org.bf.framework.autoconfigure.pay.PayProxy.PayDataTypeEnum} **/
    private String payDataType;
    /** 支付金额， 单位：分 **/
    private Long amount;
    /** 货币代码 **/
    private String currency = "CNY";

    /** 客户端IP地址 **/
    private String clientIp;

    /** 商品标题 **/
    private String subject;

    /** 商品描述信息 **/
    private String body;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 跳转通知地址 **/
    private String returnUrl;

    /** 订单失效时间, 单位：秒 **/
    private LocalDateTime expiredTime;

    /** 扩展参数 **/
    private Map<String,Object> ext = new HashMap<>();

    /** 分账模式： 0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额) **/
    private Byte divisionMode;
    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public LocalDateTime getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(LocalDateTime expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Byte getDivisionMode() {
        return divisionMode;
    }

    public void setDivisionMode(Byte divisionMode) {
        this.divisionMode = divisionMode;
    }

    public String getPayDataType() {
        return payDataType;
    }
    public void setPayDataType(String payDataType) {
        this.payDataType = payDataType;
    }
    public Object getExtValue(String key) {
        return ext.get(key);
    }
    public void setExtValue(String key,Object v) {
        this.ext.put(key,v);
    }

    /** 订单分账， 将冻结商户资金。 */
    public boolean isDivisionOrder(){
        if(getDivisionMode() != null && (DIVISION_MODE_AUTO == divisionMode || DIVISION_MODE_MANUAL == divisionMode)){
            return true;
        }
        return false;
    }

}
