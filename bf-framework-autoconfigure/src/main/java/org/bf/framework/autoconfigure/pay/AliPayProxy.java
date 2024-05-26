package org.bf.framework.autoconfigure.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.pay.model.*;
import org.bf.framework.common.result.Result;
import org.bf.framework.common.util.StringUtils;

import java.util.Map;

@Slf4j
public class AliPayProxy extends PayProxy {
    public static final String CERT_MODEL_KEY = "certModel";
    public static final String SIGN_TYPE_KEY = "signType";
    public static final String ALIPAY_PUBLIC_KEY = "aliPayPublicKey";
    public static final String ALIPAY_ROOT_CERT_KEY = "alipayRootCert";
    public static final String ALIPAY_URL = "https://openapi.alipay.com/gateway.do";
    public static final String SANDBOX_ALIPAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    public static final String OAUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";
    public static final String SANDBOX_OAUTH_URL = "https://openapi-sandbox.dl.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";
    public static final String APP_TO_APP_AUTH_URL = "https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";
    public static final String SANDBOX_APP_TO_APP_AUTH_URL = "https://openapi-sandbox.dl.alipaydev.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";
    public static final String SIGN_TYPE_RSA = "RSA";
    public static final String SIGN_TYPE_RSA2 = "RSA2";
    public static final String FORMAT = "json";
    public static final String CHARSET = "UTF-8";
    public static final String CRT = ".crt";
    private AlipayClient client;
    private boolean certModel;
    public AliPayProxy(PayProperties cfg){
        super(cfg);
        String isCert = cfg.getConfigByKey(CERT_MODEL_KEY);
        String serverUrl = cfg.isSandbox() ? SANDBOX_ALIPAY_URL : ALIPAY_URL;
        String signType = cfg.getConfigByKey(SIGN_TYPE_KEY);
        if(StringUtils.isBlank(signType)) {
            signType = SIGN_TYPE_RSA2;
        }
        String alipayPublicKey = cfg.getConfigByKey(ALIPAY_PUBLIC_KEY);
        if (StringUtils.isBlank(isCert) || !"true".equals(isCert)) {
            client = new DefaultAlipayClient(serverUrl, cfg.getAppId(), cfg.getPrivateKey(),FORMAT, CHARSET, alipayPublicKey, signType);
            return;
        }
        this.certModel = true;
        String appPublicCert = cfg.getConfigByKey(APP_CERT_KEY);
        String alipayPublicCert = cfg.getConfigByKey(PLATFORM_CERT_KEY);
        String alipayRootCert = cfg.getConfigByKey(ALIPAY_ROOT_CERT_KEY);
        if(StringUtils.hasBlank(appPublicCert,alipayPublicCert,alipayRootCert)) {
            throw new RuntimeException("构建 AlipayClient 失败,需要证书相关配置");
        }
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(serverUrl);
        certAlipayRequest.setAppId(cfg.getAppId());
        certAlipayRequest.setPrivateKey(cfg.getPrivateKey());
        certAlipayRequest.setFormat(FORMAT);
        certAlipayRequest.setCharset(CHARSET);
        certAlipayRequest.setSignType(signType);
        if(StringUtils.endsWithIgnoreCase(appPublicCert,CRT)) {
            certAlipayRequest.setCertPath(appPublicCert);
        } else {
            certAlipayRequest.setCertContent(appPublicCert);
        }
        if(StringUtils.endsWithIgnoreCase(alipayPublicCert,CRT)) {
            certAlipayRequest.setAlipayPublicCertPath(alipayPublicCert);
        } else {
            certAlipayRequest.setAlipayPublicCertContent(alipayPublicCert);
        }
        if(StringUtils.endsWithIgnoreCase(alipayRootCert,CRT)) {
            certAlipayRequest.setRootCertPath(alipayRootCert);
        } else {
            certAlipayRequest.setRootCertContent(alipayRootCert);
        }
        try {
            client = new DefaultAlipayClient(certAlipayRequest);
        } catch (AlipayApiException e) {
            log.error("error" ,e);
            client = null;
        }
    }

    @Override
    public Result<UnifiedOrderResponse> unifiedOrder(UnifiedOrderRequest req) {
        return null;
    }
    @Override
    public Result<PayNoticeResponse> payNotice(String body, Map<String,String> header, NoticeTypeEnum noticeTypeEnum) {
        return null;
    }

    @Override
    public Result<RefundResponse> refund(RefundRequest req) {
        return null;
    }
}
