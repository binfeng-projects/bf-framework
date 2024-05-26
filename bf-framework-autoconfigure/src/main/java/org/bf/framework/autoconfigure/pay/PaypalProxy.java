package org.bf.framework.autoconfigure.pay;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.pay.model.*;
import org.bf.framework.common.result.Result;

import java.util.Map;

@Slf4j
public class PaypalProxy extends PayProxy {
    private PayPalHttpClient client;

    public PaypalProxy(PayProperties cfg){
        super(cfg);
        PayPalEnvironment environment = null;
        if (cfg.isSandbox()) {
            environment = new PayPalEnvironment.Sandbox(cfg.getAppId(), cfg.getAppSecret());
        } else {
            environment = new PayPalEnvironment.Live(cfg.getAppId(), cfg.getAppSecret());
        }
        client = new PayPalHttpClient(environment);
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
