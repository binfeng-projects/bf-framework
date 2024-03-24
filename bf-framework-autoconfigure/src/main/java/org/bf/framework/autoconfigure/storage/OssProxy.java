package org.bf.framework.autoconfigure.storage;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.storage.StorageProperties;
import org.bf.framework.boot.support.storage.StorageProxy;
import org.bf.framework.common.util.IOUtils;
import org.bf.framework.common.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class OssProxy extends StorageProxy {
    private OSSClient client;

    public OssProxy(StorageProperties cfg){
        super(cfg);
        if(StringUtils.isBlank(cfg.getAccessKeyId())){
            throw new IllegalStateException("accessKeyId null");
        }
        if(StringUtils.isBlank(cfg.getAccessKeySecret())){
            throw new IllegalStateException("accessKeySecret null");
        }
        if(StringUtils.isBlank(cfg.getInternalEndpoint())){
            throw new IllegalStateException("oss internalEndpoint null");
        }
        CredentialsProvider cp = new DefaultCredentialProvider(cfg.getAccessKeyId(), cfg.getAccessKeySecret());
        this.client = new OSSClient(cfg.getInternalEndpoint(), cp, null);
    }

    @Override
    public String formatEndpoint(StorageProperties storageProperties) {
        return storageProperties.getInternalEndpoint();
    }
    /**
     * 如果封装的API不满足需求，可以得到原生的client使用
     * @return
     */
    public OSSClient getClient(){
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
            OSSObject obj = client.getObject(bucketName, key);
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
