package org.bf.framework.autoconfigure.pay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bf.framework.autoconfigure.pay.model.*;
import org.bf.framework.common.base.IdStringEnum;
import org.bf.framework.common.result.Result;

import java.util.Map;

public abstract class PayProxy {

    public static final String APP_CERT_KEY = "appPublicCert";
    public static final String PLATFORM_CERT_KEY = "platformPublicCert";
    public static final String URL_RETURN_PATH = "/api/pay/return/";
    public static final String URL_NOTIFY_PATH = "/api/pay/notify/";
    public static final String URL_REFUND_NOTIFY_PATH = "/api/refund/notify/";
    public static final String URL_JUMP_PATH = "/FORJUMP_";
    public static final String URL_PAY_HTML = "/api/common/html/";

    public static final int PLATFORM_SUCCESS = 0; //接口正确返回： 业务状态已经明确成功
    public static final int PLATFORM_WAITING = 100; //接口正确返回： 上游处理中， 需通过定时查询/回调进行下一步处理
    public static final int PLATFORM_UNKNOWN = -1; //接口超时，或网络异常等请求， 或者返回结果的签名失败： 状态不明确 ( 上游接口变更, 暂时无法确定状态值 )
    public static final int PLATFORM_ERROR = 300; //三方系统异常
    public static final int PLATFORM_FAIL = 400; //接口正确返回： 业务状态已经明确失败
    public static final int LOCAL_SYS_ERROR = 500; //本地系统异常

    protected PayProperties cfg;

    public PayProxy(PayProperties cfg) {
        this.cfg = cfg;
    }

    protected String getNotifyUrl() {
        return cfg.getSiteDomain() + URL_NOTIFY_PATH + cfg.getPayPlatform();
    }

    protected String getNotifyUrl(String payOrderId) {
        return cfg.getSiteDomain() + URL_NOTIFY_PATH + cfg.getPayPlatform() + "/" + payOrderId;
    }

    protected String getRefundNotifyUrl(String refundId) {
        return cfg.getSiteDomain() + URL_REFUND_NOTIFY_PATH + cfg.getPayPlatform() + "/" + refundId;
    }
    protected String getReturnUrl() {
        return cfg.getSiteDomain() + URL_RETURN_PATH + cfg.getPayPlatform();
    }

    protected String getReturnUrl(String payOrderId) {
        return cfg.getSiteDomain() + URL_RETURN_PATH + cfg.getPayPlatform() + "/" + payOrderId;
    }

    protected String getReturnUrlForJump(String payOrderId) {
        return cfg.getSiteDomain() + URL_RETURN_PATH + cfg.getPayPlatform() + URL_JUMP_PATH + payOrderId;
    }

    /**
     * 通知类型
     **/
    public enum NoticeTypeEnum {
        SYNC_JUMP, //同步跳转
        ASYNC_NOTIFY //异步回调
    }

    @Getter
    @AllArgsConstructor
    public enum PayDataTypeEnum implements IdStringEnum {
        REDIRECT_URL("redirectUrl", "跳转链接"),
        FORM("form", "表单提交"),
        APP("app", "app"),
        CODE_URL("codeUrl", "二维码URL"),
        CODE_IMG_URL("codeImgUrl", "二维码图片URL");
        /**
         * 唯一标识
         */
        private final String id;
        /**
         * 描述
         */
        private final String remark;

    }

    /**
     * 统一下单
     */
    public abstract Result<UnifiedOrderResponse> unifiedOrder(UnifiedOrderRequest req);

    /**
     * 支付通知，会封装掉验签，并返回三方的通知状态
     * body就是通知的请求体，header是http请求的header(有些参数可能会从head传递)
     */
    public abstract Result<PayNoticeResponse> payNotice(String body, Map<String, String> header, NoticeTypeEnum noticeTypeEnum);

    /**
     * 退款，可多次
     */
    public abstract Result<RefundResponse> refund(RefundRequest req);
}

