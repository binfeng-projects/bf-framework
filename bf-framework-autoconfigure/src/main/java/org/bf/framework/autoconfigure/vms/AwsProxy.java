package org.bf.framework.autoconfigure.vms;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.vms.VmsProperties;
import org.bf.framework.boot.support.vms.VmsProxy;

@Slf4j
public class AwsProxy extends VmsProxy {
    private AmazonS3 client;
    public AwsProxy(VmsProperties config){
        super(config);
    }
    @Override
    public String formatEndpoint(VmsProperties cfg) {
        return null;
    }
    /**
     * 如果封装的API不满足需求，可以得到原生的client使用
     * @return
     */
    public AmazonS3 getClient(){
        return client;
    }
    @Override
    public String callPhone(String mobile,String text,String templateId) {
        return null;
    }
}
