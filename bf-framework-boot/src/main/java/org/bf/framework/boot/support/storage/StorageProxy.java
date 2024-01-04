package org.bf.framework.boot.support.storage;

import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@Slf4j
public abstract class StorageProxy {
    private static final char SEPARATOR_CHAR = '/';

    protected String defaultBucketName;

    protected String endpoint;

    protected StorageProperties storageProperties;
    public StorageProxy(StorageProperties storageProperties){
        if(storageProperties == null) {
            throw new RuntimeException("config empty");
        }
        assert StringUtils.isNotBlank(storageProperties.getPlatform());
        assert StringUtils.isNotBlank(storageProperties.getBucketName());
        this.storageProperties = storageProperties;
        this.defaultBucketName = storageProperties.getBucketName();
        if(CLOUD_PLATFORM_ALIYUN.equals(storageProperties.getPlatform())){

        } else if(CLOUD_PLATFORM_AWS.equals(storageProperties.getPlatform())){

        } else if(CLOUD_PLATFORM_TENCENT.equals(storageProperties.getPlatform())){

        } else {
            throw new RuntimeException("unknown cloud platform " + storageProperties.getPlatform());
        }
        this.endpoint = formatEndpoint(storageProperties);
    }

    public abstract String formatEndpoint(StorageProperties storageProperties);

    /**
     * 上传文件, 使用 applicationName/yyyy-MM-dd/fileName 作为新文件名
     * 同名重复上传会覆盖
     */
    public String defaultBucketWithUrl(String appName,InputStream inputStream,String fileName){
        return uploadBucketWithUrl(defaultBucketName,appName,inputStream,fileName);
    }


    /**
     * 使用默认 bucket
     * 上传文件, 使用 applicationName/yyyy-MM-dd/fileName 作为新文件名
     */
    public String defaultBucketWithKey(String appName,InputStream inputStream,String fileName){
        return uploadWithFileNameKey(defaultBucketName,appName,inputStream,fileName);
    }

    /**
     * 上传文件，并返回用于下载文件的url
     * 文件标识 key = appName/yyyy-MM-dd/fileName
     */
    public String uploadBucketWithUrl(String bucketName,String appName,InputStream inputStream,String fileName) {
        String key = uploadWithFileNameKey(bucketName,appName,inputStream,fileName);
        return getDownloadUrl(bucketName,key);
    }

    /**
     * 返回文件标识 key = appName/yyyy-MM-dd/fileName
     */
    public String uploadWithFileNameKey(String bucketName,String appName,InputStream inputStream,String fileName) {
        Supplier<String> keySupplier = () -> getUniqueKey(appName,fileName);
        return uploadBucket(bucketName,inputStream,keySupplier);
    }

    /**
     * 上传文件
     * 返回文件唯一标识，key = appName/yyyy-MM-dd/hash(fileContent)
     */
    public String uploadWithContentMd5Key(String bucketName,String appName,InputStream inputStream,String fileName) {
        Supplier<String> keySupplier = () -> {
            try {
                return getUniqueKeyByHash(appName,fileName,inputStream);
            } catch (Exception e) {
                log.error("gen unique key error",e);
            }
            return null;
        };
        return uploadBucket(bucketName,inputStream,keySupplier);
    }

    public String uploadDefaultBucket(InputStream inputStream, Supplier<String> keySupplier) {
        return uploadDefaultBucket(inputStream,keySupplier,null);
    }
    public String uploadDefaultBucket(InputStream inputStream, Supplier<String> keySupplier,String savedAsName) {
        return uploadBucket(defaultBucketName,inputStream,keySupplier,savedAsName);
    }
    public String uploadBucket(String bucketName,InputStream inputStream, Supplier<String> keySupplier) {
        String key = keySupplier.get();
        if(StringUtils.isBlank(key)){
            throw new RuntimeException("key empty");
        }
        doUpload(bucketName,key,inputStream,null);
        return key;
    }
    public String uploadBucket(String bucketName,InputStream inputStream,Supplier<String> keySupplier,String savedAsName){
        String key = keySupplier.get();
        if(StringUtils.isBlank(key)){
            throw new RuntimeException("key empty");
        }
        doUpload(bucketName,key,inputStream,savedAsName);
        return key;
    }
    public abstract void doUpload(String bucketName,String key, InputStream inputStream, String savedAsName);

    public InputStream download(String key){
        return doDownload(defaultBucketName,key);
    }

    public abstract InputStream doDownload(String bucketName,String key);

    private String getUniqueKey(String appName,String fileName){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return appName + SEPARATOR_CHAR + dateFormat.format(new Date()) + SEPARATOR_CHAR + fileName;
    }

    private String getUniqueKeyByHash(String appName,String fileName,InputStream inputStream)
        throws IOException {
        String filenameExtension = fileName.substring(fileName.lastIndexOf("."));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        bufferedInputStream.mark(Integer.MAX_VALUE);
        String hash = MD5.create().digestHex(bufferedInputStream);
        //再次使用inputStream
        bufferedInputStream.reset();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return appName + SEPARATOR_CHAR + dateFormat.format(new Date()) + SEPARATOR_CHAR + hash + filenameExtension;
    }
    public String getDownloadUrl(String bucketName,String key) {
        if (StringUtils.isBlank(key)) {
            return "";
        }
        return String.format("http://%s.%s/%s", bucketName, endpoint, key);
    }
    public String getDownloadUrl(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return String.format("http://%s.%s/%s", defaultBucketName, endpoint, key);
    }
    public String defaultBucketName() {
        return defaultBucketName;
    }
}
