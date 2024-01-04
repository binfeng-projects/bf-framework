package org.bf.framework.autoconfigure.storage;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.EC2MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bf.framework.boot.support.storage.StorageProperties;
import org.bf.framework.boot.support.storage.StorageProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
@Slf4j
public class S3Proxy extends StorageProxy {
    private AmazonS3 client;
    public S3Proxy(StorageProperties cfg) {
        super(cfg);
        // 自动感知region => endpoint
        // 使用默认的DefaultAWSCredentialsProviderChain 进行授权判断，如果指定了secretKeyId and secretKey，则优先使用
        if(StringUtils.isNotBlank(cfg.getAccessKeyId())){
            System.setProperty(SDKGlobalConfiguration.ACCESS_KEY_SYSTEM_PROPERTY,cfg.getAccessKeyId());
        }
        if(StringUtils.isNotBlank(cfg.getAccessKeySecret())){
            System.setProperty(SDKGlobalConfiguration.SECRET_KEY_SYSTEM_PROPERTY,cfg.getAccessKeySecret());
        }
        // 使用默认的 DefaultAwsRegionProviderChain 进行region判断，如果显示指定了region，则优先使用
        if(StringUtils.isNotBlank(cfg.getRegion())){
            System.setProperty(SDKGlobalConfiguration.AWS_REGION_SYSTEM_PROPERTY,cfg.getRegion());
        }
        this.client =  AmazonS3ClientBuilder.standard().build();
    }

    /**
     * 如果封装的API不满足需求，可以得到原生的client使用
     * @return
     */
    public AmazonS3 getClient(){
        return client;
    }

    @Override
    public String formatEndpoint(StorageProperties storageProperties) {
        return "s3." + EC2MetadataUtils.getEC2InstanceRegion() + ".amazonaws.com.cn";
    }

    public void doUpload(String bucketName, String key, InputStream inputStream, String savedAsName){
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

    public InputStream doDownload(String bucketName, String key) {
        try {
            S3Object s3Object = client.getObject(bucketName, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = s3Object.getObjectContent()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                s3Object.close();
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
