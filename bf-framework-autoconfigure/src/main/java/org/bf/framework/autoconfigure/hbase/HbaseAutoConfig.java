package org.bf.framework.autoconfigure.hbase;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.bf.framework.autoconfigure.batch.BatchAutoConfig;
import org.bf.framework.autoconfigure.hadoop.HadoopAutoConfig;
import org.bf.framework.autoconfigure.hadoop.HadoopProperties;
import org.bf.framework.autoconfigure.hadoop.HadoopProxy;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration(after = { HadoopAutoConfig.class,BatchAutoConfig.class})
@ConditionalOnMissingBean(value = HbaseAutoConfig.class)
@EnableConfig(HbaseAutoConfig.class)
@ConditionalOnClass({Connection.class})
@Slf4j
public class HbaseAutoConfig implements EnableConfigHandler<HbaseProperties> {

    public static final String PREFIX = PREFIX_HBASE;

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public HbaseProperties bindInstance(Map<String, Object> properties) {
        return new HbaseProperties();
    }

    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        //参考 SpringHadoopConfiguration
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        HbaseProperties cfg = (HbaseProperties) YamlUtil.getConfigBind(map);
        HbaseProxy proxy = createConfig(cfg);
        if(proxy != null){
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(HbaseProxy.class).setBean(proxy));
        }
        return result;
    }

    public static HbaseProxy createConfig(HbaseProperties cfg) {
        if (cfg == null || StringUtils.isBlank(cfg.getZookeeper())) {
            return null;
        }
        String hadoopBeanPrefix = YamlUtil.prefixToBeanName(PREFIX_HADOOP + DOT + cfg.getHadoopRef());
        Configuration hadoopCfg = null;
        HadoopProxy hadoopProxy = SpringUtil.getBean(hadoopBeanPrefix + BEAN_HADOOPPROXY, HadoopProxy.class);
        if (hadoopProxy == null) {
            HadoopProperties hadoopProperties = YamlUtil.configBind(PREFIX_HADOOP, cfg.getHadoopRef(), new HadoopProperties());
            hadoopCfg = HadoopAutoConfig.createConfig(hadoopProperties);
        } else {
            hadoopCfg = hadoopProxy.getConfiguration();
        }
        if (hadoopCfg == null) {
            hadoopCfg = HBaseConfiguration.create();
        }
        String hbaseZk = cfg.getZookeeper();
        int port = hadoopCfg.getInt(HConstants.ZOOKEEPER_CLIENT_PORT, HConstants.DEFAULT_ZOOKEEPER_CLIENT_PORT);
        hadoopCfg.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, hadoopCfg.getInt(HConstants.CLIENT_ZOOKEEPER_CLIENT_PORT, port));
        Configuration hbaseConfig = HBaseConfiguration.create(hadoopCfg);
        int threadNum = Runtime.getRuntime().availableProcessors() * 2;
        log.info("------------hbaseZk: " + hbaseZk + "--------------threadNum: " + threadNum + "------------");
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(threadNum, threadNum, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
        try {
            Connection connection = ConnectionFactory.createConnection(hbaseConfig, threadPool);
            HbaseProxy proxy = new HbaseProxy(connection);
            if(StringUtils.isNotBlank(cfg.getTableName())) {
                proxy.setTableName(cfg.getTableName());
            }
            if(StringUtils.isNotBlank(cfg.getFamilyName())) {
                proxy.setFamilyByte(Bytes.toBytes(cfg.getFamilyName()));
            }
            return proxy;
        } catch (IOException ie) {
            log.error("init hbase config error", ie);
            return null;
        }
    }
}
