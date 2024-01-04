package org.bf.framework.autoconfigure.vms;

import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.vms.VmsProperties;
import org.bf.framework.boot.support.vms.VmsProxy;
import org.bf.framework.common.util.JSON;
import org.bf.framework.common.util.StringUtils;

/**
 * 后台模版配置,例如
 告警通知。${text}
 */
@Slf4j
public class AliyunProxy extends VmsProxy {
    private Client client;
    public AliyunProxy(VmsProperties cfg){
        super(cfg);
        Config aliCfg = new Config()
                .setAccessKeyId(cfg.getAccessKeyId())
                .setAccessKeySecret(cfg.getAccessKeySecret())
                .setRegionId(cfg.getRegion());

        // 访问的域名
        aliCfg.endpoint = formatEndpoint(cfg);
        try {
            this.client = new Client(aliCfg);
        } catch (Exception e) {
            throw new RuntimeException("init client error") ;
        }
    }
    /**
     * 如果封装的API不满足需求，可以得到原生的client使用
     * @return
     */
    public Client getClient(){
        return client;
    }
    @Override
    public String formatEndpoint(VmsProperties cfg) {
        return "dyvmsapi.aliyuncs.com";
    }

    @Override
    public String callPhone(String mobile,String text,String templateId) {
        SingleCallByTtsRequest request =  new SingleCallByTtsRequest()
                .setCalledNumber(mobile)
                .setTtsParam(text)
                .setSpeed(-200)
                .setTtsCode(templateId);
        if (StringUtils.isNotBlank(config.getCallShowNumber())) {
            request.setCalledShowNumber(config.getCallShowNumber());
        }
        try {
            SingleCallByTtsResponse response = client.singleCallByTts(request);
            log.info("[AliyunProxy] request = {}, response = {}", JSON.toJSONString(request), JSON.toJSONString(response));
            if(!"OK".equalsIgnoreCase(response.getBody().getCode())) {
                return String.format("mobile %s, code: %s, errmsg %s", mobile, response.getBody().getCode(), response.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        //调用成功就认为成功
        return null;
    }
}
