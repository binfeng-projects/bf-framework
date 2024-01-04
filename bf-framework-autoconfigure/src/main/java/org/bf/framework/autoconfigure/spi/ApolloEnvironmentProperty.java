package org.bf.framework.autoconfigure.spi;

import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.spi.EnvironmentPropertyPostProcessor;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.util.JSON;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.bf.framework.common.util.http.HttpUtil;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@Slf4j
public class ApolloEnvironmentProperty implements EnvironmentPropertyPostProcessor {

    private static final String PREFIX = PREFIX_APOLLO;
    private static final String LOCAL_CONFIG_FILE_NAME = "apollo-common.properties";
    @Override
    public Map<String, Object> simpleProperties() {
        Map<String, Object> result = MapUtils.newHashMap();
        String apolloMeta = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + "meta");
        if(StringUtils.isBlank(apolloMeta)) {
            return result;
        }
//        System.setProperty(ApolloClientSystemConsts.APOLLO_META, apolloMeta);
        if (SpringUtil.devOrTest()){ //测试环境会有本地连接的需要，需要和ApolloMeta一样能通
            result.put(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE, apolloMeta);
        }
        result.put(ApolloClientSystemConsts.APP_ID, SpringUtil.appName());
        // 初始化 apollo.cacheDir
        result.put(ApolloClientSystemConsts.APOLLO_CACHE_DIR, "/opt/.apollo/cache/");
        result.put(ApolloClientSystemConsts.APOLLO_CLUSTER, SpringUtil.currentCluster());
        String commonNamespace = SpringUtil.getEnvironment().getProperty(PREFIX + DOT + "common-namespace");
        if(StringUtils.isNotBlank(commonNamespace)) {
            for(String ns: commonNamespace.split(",")){
                Map<String, Object> nsResult = getNamespaceConfig(ns.trim(),apolloMeta);
                result.putAll(nsResult);
            }
            //TODO fix
//            if(MapUtils.isEmpty(result)) {
//                result = loadFromLocal();
//                if(MapUtils.isEmpty(result)){
//                    throw new RuntimeException("load common config error");
//                }
//            }else{
//                saveToLocal(result);
//            }
        }
        result.put(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, true);
        return result;
    }

    /**
     * /断是否有Apollo的meta配置，如果有，注入一些必要的Apollo配置到System中，然后开启公共namespace的load
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE -1;
    }

    public static Map<String, Object> getNamespaceConfig(String namespace,String apolloMeta){
        List<String> clusters = new ArrayList<>(4);
        String clusterName = SpringUtil.currentCluster();
        if (!DEFAULT.equals(clusterName)){
            clusters.add(DEFAULT);
        }
        clusters.add(clusterName);
        String[] servers = apolloMeta.split(",");
//        String appName = "gwadmin";
        String appName = SpringUtil.appName();
        Map<String, Object> appConfig = MapUtils.newHashMap();
        for(String cluster: clusters){
            try {
                Map<String, Object> clusterConfig = pollConfig(servers, appName, namespace, cluster);
                if (CollectionUtils.isEmpty(clusterConfig)){
                    continue;
                }
                appConfig.putAll(clusterConfig);
            } catch (Exception e) {
                log.error("pull apollo config error",e);
            }
        }
        return appConfig;
    }

    private static Map<String, Object> pollConfig(String[] servers, String appName, String namespace, String clusterName){
        Map<String, Object> config = MapUtils.newHashMap();
        for (String meta : servers){
            StringBuilder sb = new StringBuilder();
            sb.append(meta);
            sb.append("/configs/");
            sb.append(appName).append("/");
            sb.append(clusterName).append("/");
            sb.append(namespace);
            String httpResult = HttpUtil.get(sb.toString(),null,null);
            if (StringUtils.isBlank(httpResult)){
                continue;
            }
            ApolloConfig  defConfig = JSON.parseObject(httpResult, ApolloConfig.class);
            if (defConfig != null && defConfig.getConfigurations() != null){
                config.putAll(defConfig.getConfigurations());
                break;
            }
        }
        return config;
    }
    /**
     * 从缓存在磁盘中数据恢复公共配置
     */
    private Map<String, Object> loadFromLocal() {
        String localConfigFileDir = getLocalConfigDir();
        String fullName = localConfigFileDir + LOCAL_CONFIG_FILE_NAME;
        File file = new File(fullName);
        Properties properties = new Properties();
        if (file.isFile() && file.canRead()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
                log.warn("[apollo init] Loading local config file {} successfully!", file.getAbsolutePath());
            } catch (IOException ex) {
                log.error("failed to loading common config from local cache file : " + file.getAbsolutePath(), ex);
            }
        }
        if(properties.isEmpty()){
            return Collections.emptyMap();
        }
        Map<String, Object> resultMap = new HashMap<>(properties.size());
        for(Object key : properties.keySet()){
            resultMap.put(key.toString(),properties.get(key));
        }
        return resultMap;
    }

    private static String getLocalConfigDir() {
        return System.getProperty(ApolloClientSystemConsts.APOLLO_CACHE_DIR)+System.getProperty(ApolloClientSystemConsts.APP_ID)+"/config-cache/";
    }
    /**
     * 公共配置落磁盘用于容灾
     */
    private static void saveToLocal(Map<String,Object> config) {
        if(CollectionUtils.isEmpty(config)){
            return;
        }
        String localConfigFileDir = getLocalConfigDir();
        String fullName = localConfigFileDir + LOCAL_CONFIG_FILE_NAME;
        try {
            Path path = Paths.get(localConfigFileDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }catch (IOException ioe){
            log.error("failed to create base dir {}",localConfigFileDir);
            return;
        }
        try {
            File file = new File(fullName);
            OutputStream out = new FileOutputStream(file);
            Properties p = new Properties();
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                p.put(entry.getKey(), entry.getValue());
            }
            p.store(out, "common apollo config");
        } catch (IOException ex) {
            log.error("failed to save common config properties to local cache file : " + fullName, ex);
        }
    }
}
