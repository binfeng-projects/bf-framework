package org.bf.framework.autoconfigure.pay.model.wechatpay;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class WechatPayOrderRequest implements Serializable {

    /** 服务商应用ID */
    @SerializedName(value = "sp_appid")
    private String spAppid;

    /** 服务商户号 */
    @SerializedName(value = "sp_mchid")
    private String spMchid;

    /** 子商户号 */
    @SerializedName(value = "sub_mchid")
    private String subMchid;

    /** 子商户应用ID */
    @SerializedName(value = "sub_appid")
    private String subAppid;

    /** 普通商户： 商户号 */
    @SerializedName(value = "mchid")
    private String mchid;

    /** 普通商户： appId */
    @SerializedName(value = "appid")
    private String appid;

    /** 商户订单号 */
    @SerializedName(value = "out_trade_no")
    private String outTradeNo;

    /** 商品描述 */
    private String description;

    /** 交易结束时间 */
    @SerializedName(value = "time_expire")
    private String timeExpire;

    /** 附加数据 */
    private String attach;

    /** 通知地址 */
    @SerializedName(value = "notify_url")
    private String notifyUrl;

    /** 订单优惠标记 */
    @SerializedName(value = "goods_tag")
    private String goodsTag;

    /** 结算信息 */
    @SerializedName(value = "settle_info")
    private SettleInfo settleInfo;

    /** 订单金额 */
    private Amount amount;

    /** 支付者 */
    private Payer payer;

    /** 场景信息 */
    @SerializedName(value = "scene_info")
    private SceneInfo sceneInfo;

    /**  场景信息 **/
    @Data
    @Accessors(chain = true)
    public static class SceneInfo{
        /** 用户终端IP */
        @SerializedName(value = "payer_client_ip")
        private String payerClientIp;
        /** 商户端设备号 */
        @SerializedName(value = "device_id")
        private String deviceId;
        /** 商户端设备号 */
        @SerializedName(value = "h5_info")
        private H5Info h5Info;
        /** H5场景信息 */
        @Data
        @Accessors(chain = true)
        public static class H5Info{
            /** 场景类型 */
            @SerializedName(value = "type")
            private String type;
        }
    }
    /** 结算信息 **/
    @Data
    @Accessors(chain = true)
    public static class SettleInfo{
        /** 用户服务标识 */
        @SerializedName(value = "profit_sharing")
        private Boolean profitSharing;
    }
    /** 支付者 **/
    @Data
    @Accessors(chain = true)
    public static class Payer{

        /** 普通商户的 openid*/
        @SerializedName(value = "openid")
        private String openId;

        /** 用户服务标识 */
        @SerializedName(value = "sp_openid")
        private String spOpenid;

        /** 用户子标识 */
        @SerializedName(value = "sub_openid")
        private String subOpenid;
    }
    @Data
    @Accessors(chain = true)
    public static class Amount{

        /** 金额 */
        private Integer total;

        /** 货币 */
        private String currency;
    }

}
