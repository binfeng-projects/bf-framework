package org.bf.framework.autoconfigure.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bf.framework.boot.support.storage.StorageProperties;
import org.bf.framework.boot.support.storage.StorageProxy;
import org.bf.framework.common.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class CosProxy extends StorageProxy {
    private COSClient client;

    public CosProxy(StorageProperties cfg){
        super(cfg);
        if(StringUtils.isBlank(cfg.getAccessKeyId())){
            throw new IllegalStateException("accessKeyId null");
        }
        if(StringUtils.isBlank(cfg.getAccessKeySecret())){
            throw new IllegalStateException("accessKeySecret null");
        }
        if(StringUtils.isBlank(cfg.getRegion())){
            cfg.setRegion("ap-shanghai");
        }
        COSCredentials cred = new BasicCOSCredentials(cfg.getAccessKeyId(), cfg.getAccessKeySecret());
// 设置 bucket 的地域, COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
        Region region = new Region(cfg.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
// 这里建议设置使用 https 协议 从 5.6.54 版本开始，默认使用了 https
//        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.client = new COSClient(cred, clientConfig);
    }

    @Override
    public String formatEndpoint(StorageProperties storageProperties) {
        return "cos." + storageProperties.getRegion() + ".myqcloud.com";
    }
    /**
     * 如果封装的API不满足需求，可以得到原生的client使用
     * @return
     */
    public COSClient getClient(){
        return client;
    }

    public void doUpload(String bucketName,String key, InputStream inputStream, String savedAsName){
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setCacheControl("max-age=315360000");
        // 下载时展示的信息
        objectMetadata.setContentDisposition("inline;filename=" + (StringUtils.isNotBlank(savedAsName) ? savedAsName : key));
        try {
            objectMetadata.setContentLength(inputStream.available());
            PutObjectResult putResult = client.putObject(bucketName,key,inputStream,objectMetadata);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public InputStream doDownload(String bucketName,String key) {
        try {
            COSObject obj = client.getObject(bucketName, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = obj.getObjectContent()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                IOUtils.closeQuietly(obj);
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
