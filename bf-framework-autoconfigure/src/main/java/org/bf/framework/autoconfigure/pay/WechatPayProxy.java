package org.bf.framework.autoconfigure.pay;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLEncodeUtil;
import com.alibaba.fastjson2.JSON;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayMicropayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayMicropayResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.bf.framework.autoconfigure.pay.model.*;
import org.bf.framework.autoconfigure.pay.model.wechatpay.WechatPayOrderRequest;
import org.bf.framework.common.base.IdStringEnum;
import org.bf.framework.common.result.Result;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WechatPayProxy extends PayProxy {
    public static final String TRADE_TYPE_MICROPAY = "MICROPAY";
    public static final String MERCHANT_SERIALNUMBER_KEY = "serialNo";
    public static final String APIV3_KEY = "apiV3Key";
    /** 小程序或者jsapi需要传openid*/
    public static final String PAY_ORDER_OPENID = "openId";
    /** 扫条码支付需要传AuthCode*/
    public static final String PAY_ORDER_AUTHCODE = "authCode";

    /** 支付回调的时候需要解析的参数*/
    public static final String PAY_NOTICE_TIMESTAMP = "Wechatpay-Timestamp";
    public static final String PAY_NOTICE_NONCE = "Wechatpay-Nonce";
    public static final String PAY_NOTICE_SERIAL = "Wechatpay-Serial";
    public static final String PAY_NOTICE_SIGNATURE = "Wechatpay-Signature";
    private WxPayService wxPayService;
    private WxMpService wxMpService;
    public static final String PEM = ".pem";
    public static final String P12 = ".p12";

    private static final String WECHAT_PAY_HOST = "https://api.mch.weixin.qq.com";
    public WechatPayProxy(PayProperties cfg){
        super(cfg);
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setMchId(cfg.getMchId());
        wxPayConfig.setAppId(cfg.getAppId());
//        wxPayConfig.setMchKey(mchKey); //apiKey
        wxPayConfig.setApiV3Key(cfg.getConfigByKey(APIV3_KEY));
        wxPayConfig.setCertSerialNo(cfg.getConfigByKey(MERCHANT_SERIALNUMBER_KEY));
        if(StringUtils.endsWithIgnoreCase(cfg.getPrivateKey(),PEM)) {
            wxPayConfig.setPrivateKeyPath(cfg.getPrivateKey());
        } else {
            wxPayConfig.setPrivateKeyString(cfg.getPrivateKey());
        }
        String wechatPublicCert = cfg.getConfigByKey(PLATFORM_CERT_KEY);
        if(StringUtils.endsWithIgnoreCase(wechatPublicCert,P12)) {
            wxPayConfig.setKeyPath(wechatPublicCert);
        } else {
            wxPayConfig.setKeyString(wechatPublicCert);
        }
        String appPublicCert = cfg.getConfigByKey(APP_CERT_KEY);
        if(StringUtils.endsWithIgnoreCase(appPublicCert,PEM)) {
            wxPayConfig.setPrivateCertPath(appPublicCert);
        } else {
            wxPayConfig.setPrivateCertString(appPublicCert);
        }
//        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
//                new WxPayCredentials(wxPayConfig.getMchId(), new PrivateKeySigner(wxPayConfig.getCertSerialNo(), cfg.getPrivateKey())), wxPayConfig.getApiV3Key().getBytes("utf-8"));
//        wxPayConfig.setVerifier(verifier);
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(wxPayConfig); //微信配置信息
        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(cfg.getAppId());
        wxMpConfigStorage.setSecret(cfg.getAppSecret());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage); //微信配置信息
    }
    @Override
    public Result<UnifiedOrderResponse> unifiedOrder(UnifiedOrderRequest req) {
        if(req == null || StringUtils.isBlank(req.getPayOrderId()) || StringUtils.isBlank(req.getTradeType())) {
            throw new RuntimeException("unifiedOrder error, param error");
        }
        /** 刷卡支付. 刷卡支付有单独的支付接口，不调用统一下单接口 */
        if(TRADE_TYPE_MICROPAY.equalsIgnoreCase(req.getTradeType())) {
            return microPayOder(req);
        }
        // 微信统一下单请求对象
        WechatPayOrderRequest orderRequest = genUnifiedOrderRequest(req);

        WechatTradeTypeEnum tradeType = IdStringEnum.of(req.getTradeType(),WechatTradeTypeEnum.class);
        String reqUrl = WECHAT_PAY_HOST + tradeType.getMchUrl();
        if(StringUtils.isNotBlank(cfg.getSubMchInfo())) { //特约商户
            reqUrl = WECHAT_PAY_HOST + tradeType.getPartnerUrl();
        }
        UnifiedOrderResponse response = new UnifiedOrderResponse();
        response.setPayDataType(req.getPayDataType());
        response.setPayOrderId(req.getPayOrderId());
        response.setTradeType(req.getTradeType());
        try {
            WxPayUnifiedOrderV3Result v3Result = JSON.parseObject(wxPayService.postV3(reqUrl, JSON.toJSONString(orderRequest)), WxPayUnifiedOrderV3Result.class);
            response.setPayResult(parseUnifiedOrderResponse(req,v3Result));
            return Result.of(response);
        } catch (WxPayException e) {
            log.error("wechat pay error",e);
            return Result.fail(PLATFORM_FAIL,req.getPayOrderId() + ":" + genWxPayErrInfo(e));
        }
    }

    @Override
    public Result<RefundResponse> refund(RefundRequest req){
        WxPayRefundV3Request request = new WxPayRefundV3Request();
        request.setOutTradeNo(req.getPayOrderId());
        request.setOutRefundNo(req.getRefundId());
        request.setNotifyUrl(StringUtils.isBlank(req.getNotifyUrl()) ? getRefundNotifyUrl(req.getRefundId()) : req.getNotifyUrl());
        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setRefund(req.getRefundAmount().intValue());// 退款金额
        amount.setTotal(req.getAmount().intValue());// 订单总金额
        amount.setCurrency(req.getCurrency());// 币种
        request.setAmount(amount);
        if(StringUtils.isNotBlank(cfg.getSubMchInfo())) { //特约商户
            request.setSubMchid(cfg.getSubMchInfo());
        }
        RefundResponse response = new RefundResponse();
        response.setPayOrderId(req.getPayOrderId());
        response.setRefundId(req.getRefundId());
        try {
            WxPayRefundV3Result v3Result = wxPayService.refundV3(request);
            response.setPayPlatformRefundId(v3Result.getRefundId());
            String status = v3Result.getStatus();
            response.setStatus(status);
            response.setPayPlatformOrderId(v3Result.getTransactionId());
            response.setPayPlatformUserId(v3Result.getUserReceivedAccount());
            if("SUCCESS".equals(status)) { // 退款成功
                return Result.of(response);
            }
            else if("PROCESSING".equals(status)){ // 退款处理中
                return Result.fail(PLATFORM_WAITING,"Wechat State :" + status,response);
            } else {
                return Result.fail(PLATFORM_FAIL,"Wechat State :" + status,response);
            }
        } catch (WxPayException e) {
            log.error("微信退款WxPayException异常: ", e);
            return Result.fail(PLATFORM_FAIL,req.getPayOrderId() + ":" + genWxPayErrInfo(e));
        }
    }
    @Override
    public Result<PayNoticeResponse> payNotice(String body, Map<String,String> header, NoticeTypeEnum noticeTypeEnum) {
        SignatureHeader h = new SignatureHeader();
        h.setTimeStamp(header.get(PAY_NOTICE_TIMESTAMP));
        h.setNonce(header.get(PAY_NOTICE_NONCE));
        h.setSerial(header.get(PAY_NOTICE_SERIAL));
        h.setSignature(header.get(PAY_NOTICE_SIGNATURE));
        PayNoticeResponse resp = new PayNoticeResponse();
        resp.setNoticeType(noticeTypeEnum.name());
        try {
            WxPayNotifyV3Result v3Result = wxPayService.parseOrderNotifyV3Result(body, h);
            WxPayNotifyV3Result.DecryptNotifyResult result = v3Result.getResult();
            resp.setPayOrderId(result.getOutTradeNo()); //我方的订单号
            resp.setPayPlatformOrderId(result.getTransactionId()); //三方支付平台的订单号
            WxPayNotifyV3Result.Payer payer = result.getPayer();
            if (payer != null) {
                resp.setPayPlatformUserId(payer.getOpenid()); //三方支付平台的支付用户ID
            }
            String state = result.getTradeState();
            resp.setTradeState(state);
            if("CLOSED".equals(state)
                    || "REVOKED".equals(state)
                    || "PAYERROR".equals(state)){  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
                //支付失败
                return Result.fail(PLATFORM_FAIL,"Wechat Notify State :" + state,resp);
            }
//            if ("SUCCESS".equals(state)) {
//            }
            Map<String,Object> responseBody = new HashMap<>();
            responseBody.put("code", "SUCCESS");
            responseBody.put("message", "成功");
            resp.setResponseBody(responseBody);
            return Result.of(resp);
        } catch (WxPayException e) {
            log.error("payNotice Error",e);
            throw new RuntimeException("payNotice Error");
        }
    }

    /** 刷卡支付. 刷卡支付有单独的支付接口，不调用统一下单接口 */
    private Result<UnifiedOrderResponse> microPayOder(UnifiedOrderRequest req){
        WxPayMicropayRequest request = new WxPayMicropayRequest();
        request.setOutTradeNo(req.getPayOrderId());
        request.setBody(req.getSubject());
        request.setDetail(req.getBody());
        request.setFeeType(req.getCurrency());
        request.setTotalFee(req.getAmount().intValue());
        request.setSpbillCreateIp(req.getClientIp());
        request.setAuthCode((String)req.getExtValue(PAY_ORDER_AUTHCODE));
        //订单分账， 将冻结商户资金。
        if(req.isDivisionOrder()){
            request.setProfitSharing("Y");
        }
        if(StringUtils.isNotBlank(cfg.getSubMchInfo())) { //特约商户
            request.setSubMchId(cfg.getSubMchInfo());
            request.setSubAppId(cfg.getSubAppId());
        }
        UnifiedOrderResponse response = new UnifiedOrderResponse();
        response.setPayDataType(req.getPayDataType());
        response.setPayOrderId(req.getPayOrderId());
        response.setTradeType(req.getTradeType());
        try {
            WxPayMicropayResult wxPayMicropayResult = wxPayService.micropay(request);
            response.setPayResult(wxPayMicropayResult.toString());
            return Result.of(response);
        } catch (WxPayException e) {
            //微信返回支付状态为【支付结果未知】, 需进行查单操作
            if("SYSTEMERROR".equals(e.getErrCode()) || "USERPAYING".equals(e.getErrCode()) ||  "BANKERROR".equals(e.getErrCode())){
                //轮询查询订单
                return Result.fail(PLATFORM_WAITING,req.getPayOrderId() + ":" + genWxPayErrInfo(e));
            }else {
                return Result.fail(PLATFORM_FAIL,req.getPayOrderId() + ":" + genWxPayErrInfo(e));
            }
        }
    }

    private String parseUnifiedOrderResponse(UnifiedOrderRequest req, WxPayUnifiedOrderV3Result v3Result) {
        // 普通商户
        String appId = cfg.getMchId();
        String mchId = cfg.getAppId();
        if(StringUtils.isNotBlank(cfg.getSubMchInfo())) { // 特约商户
            appId = cfg.getSubAppId();
            if(StringUtils.isBlank(appId)) {
                appId = cfg.getAppId();
            }
            mchId = cfg.getSubMchInfo();
        }
        PayDataTypeEnum payDataType = IdStringEnum.of(req.getPayDataType(),PayDataTypeEnum.class);
        WechatTradeTypeEnum tradeType = IdStringEnum.of(req.getTradeType(),WechatTradeTypeEnum.class);
        if(WechatTradeTypeEnum.APP.equals(tradeType)) {
            WxPayUnifiedOrderV3Result.AppResult appResult = v3Result.getPayInfo(TradeTypeEnum.APP, appId, mchId, wxPayService.getConfig().getPrivateKey());
            Map<String, Object> jsonRes = MapUtils.beanToMap(appResult);
            jsonRes.put("package", jsonRes.remove("packageValue"));
            return JSON.toJSONString(jsonRes);
        } else if(WechatTradeTypeEnum.MINI.equals(tradeType) || WechatTradeTypeEnum.JSAPI.equals(tradeType)) {
            WxPayUnifiedOrderV3Result.JsapiResult jsapiResult = v3Result.getPayInfo(TradeTypeEnum.JSAPI, appId, null, wxPayService.getConfig().getPrivateKey());
            Map<String, Object> jsonRes = MapUtils.beanToMap(jsapiResult);
            jsonRes.put("package", jsonRes.remove("packageValue"));
            return JSON.toJSONString(jsonRes);
        } else if(WechatTradeTypeEnum.H5.equals(tradeType)){
            String payUrl = String.format("%s&redirect_url=%s", v3Result.getH5Url(), URLEncodeUtil.encode(getReturnUrlForJump(req.getPayOrderId())));
            payUrl = String.format("%s%s%s", cfg.getSiteDomain(),URL_PAY_HTML, Base64.encode(payUrl));
//            if (PayDataTypeEnum.CODE_IMG_URL.equals(payDataType)){ //二维码图片地址
//                response.setPayResult(genScanImgUrl(payUrl));
//            }else{ // 默认都为 payUrl方式
//                response.setPayResult(payUrl);
//            }
            return payUrl;
        } else if(WechatTradeTypeEnum.NATIVE.equals(tradeType)){
//            if (PayDataTypeEnum.CODE_IMG_URL.equals(payDataType)){ //二维码图片地址
//                response.setPayResult(genScanImgUrl(v3Result.getCodeUrl()));
//            }else{ // 默认都为 payUrl方式
//                response.setPayResult(v3Result.getCodeUrl());
//            }
            return v3Result.getCodeUrl();
        }
        throw new RuntimeException("wrong trade type");
    }

    private WechatPayOrderRequest genUnifiedOrderRequest(UnifiedOrderRequest req) {
        String payOrderId = req.getPayOrderId();
        // 微信统一下单请求对象
        WechatPayOrderRequest orderRequest = new WechatPayOrderRequest();
        WechatTradeTypeEnum tradeType = IdStringEnum.of(req.getTradeType(),WechatTradeTypeEnum.class);
        String openId = (String)req.getExtValue(PAY_ORDER_OPENID);
        if(StringUtils.isBlank(cfg.getSubMchInfo())) { //如果未配置子商户信息，则是普通商户模式
            orderRequest.setMchid(cfg.getMchId());
            orderRequest.setAppid(cfg.getAppId());
        } else { // 特约商户
            orderRequest.setSpAppid(cfg.getAppId());
            orderRequest.setSpMchid(cfg.getMchId());
            orderRequest.setSubMchid(cfg.getSubMchInfo());
            orderRequest.setSubAppid(cfg.getSubAppId());
        }
        orderRequest.setSceneInfo(new WechatPayOrderRequest.SceneInfo().setPayerClientIp(req.getClientIp()));
        orderRequest.setOutTradeNo(payOrderId);
        orderRequest.setDescription(req.getSubject());
        orderRequest.setTimeExpire(String.format("%sT%s+08:00", DateUtil.format(req.getExpiredTime(), DatePattern.NORM_DATE_PATTERN), DateUtil.format(req.getExpiredTime(), DatePattern.NORM_TIME_PATTERN)));
        orderRequest.setNotifyUrl(StringUtils.isBlank(req.getNotifyUrl()) ? getNotifyUrl(payOrderId) : req.getNotifyUrl());
        orderRequest.setAmount(new WechatPayOrderRequest.Amount().setCurrency(req.getCurrency()).setTotal(req.getAmount().intValue()));

        if(WechatTradeTypeEnum.MINI.equals(tradeType) || WechatTradeTypeEnum.JSAPI.equals(tradeType)) {
            if(StringUtils.isBlank(openId)) {
                throw new RuntimeException("openid null");
            }
            if (StringUtils.isNotBlank(cfg.getSubAppId())) {
                // 用户在子商户appid下的唯一标识
                orderRequest.setPayer(new WechatPayOrderRequest.Payer().setSubOpenid(openId));
            } else if(StringUtils.isNotBlank(cfg.getSubMchInfo())) {
                orderRequest.setPayer(new WechatPayOrderRequest.Payer().setSpOpenid(openId));
            } else {
                orderRequest.setPayer(new WechatPayOrderRequest.Payer().setOpenId(openId));
            }
        } else if(WechatTradeTypeEnum.H5.equals(tradeType)){
            orderRequest.getSceneInfo().setH5Info(new WechatPayOrderRequest.SceneInfo.H5Info().setType("iOS, Android, Wap"));
        }
        //订单分账， 将冻结商户资金。
        if(req.isDivisionOrder()){
            orderRequest.setSettleInfo(new WechatPayOrderRequest.SettleInfo().setProfitSharing(true));
        }
        return orderRequest;
    }
    @Getter
    @AllArgsConstructor
    public enum WechatTradeTypeEnum implements IdStringEnum {
        APP("app", "/v3/pay/transactions/app", "/v3/combine-transactions/app", "/v3/pay/partner/transactions/app"),
        JSAPI("jsapi", "/v3/pay/transactions/jsapi", "/v3/combine-transactions/jsapi", "/v3/pay/partner/transactions/jsapi"),
        /**
         * 小程序,同jsapi
         */
        MINI("mini", "/v3/pay/transactions/jsapi", "/v3/combine-transactions/jsapi", "/v3/pay/partner/transactions/jsapi"),
        NATIVE("native", "/v3/pay/transactions/native", "/v3/combine-transactions/native", "/v3/pay/partner/transactions/native"),
        H5("h5", "/v3/pay/transactions/h5", "/v3/combine-transactions/h5", "/v3/pay/partner/transactions/h5");
        /**
         * 唯一标识
         */
        private final String id;
        /**
         * 普通商户下单url
         */
        private final String mchUrl;

        /**
         * 合并下单url
         */
        private final String combineUrl;

        /**
         * 服务商下单
         */
        private final String partnerUrl;

    }
    public static String appendErrCode(String code, String subCode){
        return StringUtils.isBlank(subCode) ? code : subCode;
    }

    public static String appendErrMsg(String msg, String subMsg){
        if(StringUtils.isNotBlank(msg) && StringUtils.isNotBlank(subMsg) ){
            return msg + "【" + subMsg + "】";
        }
        return StringUtils.isBlank(subMsg) ? msg : subMsg;
    }

    public static String genWxPayErrInfo(WxPayException wpe){
        String errCode = appendErrCode(wpe.getReturnCode(),wpe.getErrCode());
        String errMsg = appendErrMsg("OK".equalsIgnoreCase(wpe.getReturnMsg()) ? null : wpe.getReturnMsg(), wpe.getErrCodeDes());
        if(StringUtils.isBlank(errMsg)) {
            return StringUtils.isBlank(wpe.getCustomErrorMsg()) ? wpe.getMessage() : wpe.getCustomErrorMsg();
        } else {
            return errCode + " : " + errMsg;
        }
    }
}
