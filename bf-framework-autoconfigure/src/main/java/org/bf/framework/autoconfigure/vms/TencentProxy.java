package org.bf.framework.autoconfigure.vms;

import com.alibaba.fastjson2.JSON;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vms.v20200902.VmsClient;
import com.tencentcloudapi.vms.v20200902.models.SendTtsVoiceRequest;
import com.tencentcloudapi.vms.v20200902.models.SendTtsVoiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.vms.VmsProperties;
import org.bf.framework.boot.support.vms.VmsProxy;

/**
 *         //后台模版配置,例如
 *         系统通知。{1}
 */
@Slf4j
public class TencentProxy extends VmsProxy {
    private VmsClient client;

    public TencentProxy(VmsProperties config){
        super(config);
        Credential cred = new Credential(config.getAccessKeyId(), config.getAccessKeySecret());
        // 实例化一个http选项，可选，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setReqMethod("POST");
        /* SDK有默认的超时时间，非必要请不要进行调整
         * 如有需要请在代码中查阅以获取最新的默认值 */
        httpProfile.setConnTimeout(60);
        httpProfile.setEndpoint(formatEndpoint(config));
        /* 非必要步骤:
         * 实例化一个客户端配置对象，可以指定超时时间等配置 */
        ClientProfile clientProfile = new ClientProfile();
        /* SDK默认用TC3-HMAC-SHA256进行签名
         * 非必要请不要修改这个字段 */
        clientProfile.setSignMethod("TC3-HMAC-SHA256");
        clientProfile.setHttpProfile(httpProfile);
        this.client = new VmsClient(cred, config.getRegion(), clientProfile);
    }
    @Override
    public String formatEndpoint(VmsProperties cfg) {
        return "vms.tencentcloudapi.com";
    }
    /**
     * 如果封装的API不满足需求，可以得到原生的client使用
     * @return
     */
    public VmsClient getClient(){
        return client;
    }
    @Override
    public String callPhone(String mobile,String text,String templateId) {
        SendTtsVoiceRequest request = new SendTtsVoiceRequest();
        if(!mobile.startsWith("+")) {
            mobile = "+86" + mobile;
        }
        request.setCalledNumber(mobile);
        request.setTemplateParamSet(new String[] {text});
        request.setVoiceSdkAppid(config.getAppId());
        request.setTemplateId(templateId);
        try {
            SendTtsVoiceResponse response = client.SendTtsVoice(request);
            log.info("[TencentProxy] request = {}, response = {}", JSON.toJSONString(request), JSON.toJSONString(response));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        //调用成功就认为成功
        return null;
    }
}
