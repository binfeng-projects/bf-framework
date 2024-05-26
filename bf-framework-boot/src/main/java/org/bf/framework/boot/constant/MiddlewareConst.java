package org.bf.framework.boot.constant;

import org.bf.framework.common.util.CollectionUtils;

import java.util.List;

/**
 * 中间件用到的一些常量
 */
public interface MiddlewareConst {
    //-----------------------------------datasource-------------------------------
    String PREFIX_DATASOURCE = "datasource";
    String BEAN_DATASOURCE = "_DataSource";
    String BEAN_PLATFORMTRANSACTIONMANAGER = "_PlatformTransactionManager";
    //-----------------------------------jooq------------------------------
    String PREFIX_JOOQ = "jooq";
    String BEAN_DSLCONTEXT = "_DSLContext";
    //-----------------------------------mybatis------------------------------
    String PREFIX_MYBATIS = "mybatis";
    String BEAN_SQLSESSIONFACTORY = "_SqlSessionFactory";

    //-----------------------------------local-cache------------------------------
    String PREFIX_CACHE = "cache";
    String SCHEMA_CACHE_LOCAL = "local";
    //----------------------------------- redis-----------------------------------
    String PREFIX_REDIS = "redis";
    String BEAN_STRINGREDISTEMPLATE = "_StringRedisTemplate";
    String BEAN_REDISTEMPLATE = "_RedisTemplate";
    String BEAN_REDISCACHEMANAGER = "_RedisCacheManager";
    String BEAN_REDISSONCLIENT = "_RedissonClient";
    String BEAN_CACHEMANAGER = "_CacheManager";
    //----------------------------------- kafka-----------------------------------
    String PREFIX_KAFKA = "kafka";
    String BEAN_KAFKATEMPLATE = "_KafkaTemplate";
    String BEAN_KAFKALISTENERCONTAINERFACTORY = "_KafkaListenerContainerFactory";
    //----------------------------------- elasticsearch-----------------------------------
    String PREFIX_ELASTICSEARCH = "elasticsearch";
    String BEAN_ELASTICSEARCHTEMPLATE = "_ElasticsearchTemplate";
//    String DEFAULT_SEARCH_ANALYZER = "ik_max_word";
    //----------------------------------- xxl-----------------------------------
    String PREFIX_XXL = "xxl";
    String BEAN_XXLJOBEXECUTOR = "_XxlJobExecutor";
    //----------------------------------- apollo -----------------------------------
    String PREFIX_APOLLO = "apollo";
    //----------------------------------- nacos-----------------------------------
    String PREFIX_NACOS = "nacos";
    //----------------------------------- cloud platform -----------------------------------
    String CLOUD_PLATFORM_ALIYUN = "aliyun";
    String CLOUD_PLATFORM_AWS = "aws";
    String CLOUD_PLATFORM_TENCENT = "tencent";
    //----------------------------------- storage -----------------------------------
    String PREFIX_STORAGE = "storage";
    String BEAN_STORAGEPROXY = "_StorageProxy";
    //----------------------------------- vms -----------------------------------
    String PREFIX_VMS = "vms";
    String BEAN_VMSPROXY = "_VmsProxy";
    //----------------------------------- zookeeper -----------------------------------
    String PREFIX_ZOOKEEPER = "zookeeper";
    String BEAN_CURATORFRAMEWORK = "_CuratorFramework";
    //----------------------------------- dubbo -----------------------------------
    String PREFIX_DUBBO = "dubbo";
    //----------------------------------- flink -----------------------------------
    String PREFIX_FLINK = "flink";
    String BEAN_KAFKASOURCEBUILDER = "_KafkaSourceBuilder";
    String BEAN_KAFKASINKBUILDER = "_KafkaSinkBuilder";
    //----------------------------------- hadoop -----------------------------------
    String PREFIX_HADOOP = "hadoop";
    String BEAN_HADOOPPROXY = "_HadoopProxy";
//    String BEAN_FSSHELL = "_FsShell";
//----------------------------------- hbase -----------------------------------
    String PREFIX_HBASE = "hbase";
    String BEAN_HBASEPROXY = "_HbaseProxy";
//----------------------------------- hive -----------------------------------
    String PREFIX_HIVE = "hive";
    String BEAN_HIVETEMPLATE = "_HiveTemplate";
//----------------------------------- yarn -----------------------------------
    String PREFIX_YARN = "yarn";
    String BEAN_YARNPROXY = "_YarnProxy";
    String BEAN_YARNCONTAINER = "_YarnContainer";
    //----------------------------------- batch -----------------------------------
    String PREFIX_BATCH = "batch";
    String BEAN_BATCHPROXY = "_BatchProxy";
    //---------------------------- sentinel -------------------------------
    String PREFIX_SENTINEL = "sentinel";
    //----------------------------------- rocketmq -----------------------------------
    String PREFIX_ROCKETMQ = "rocketmq";
    String BEAN_ROCKETMQTEMPLATE = "_RocketMQTemplate";
//  ------------------------------ 支付配置 --------------------------------
    String PAY_PLATFORM_ALI = "alipay";
    String PAY_PLATFORM_WECHAT= "wechatpay";
    String PAY_PLATFORM_PAYPAL = "paypal";
    //----------------------------------- pay -----------------------------------
    String PREFIX_PAY = "pay";
    String BEAN_PAYPROXY = "_PayProxy";
    //----------------------------------- 通用 -----------------------------------
    String ENABLED = "enabled";
    String URL = "url";
    String TYPE = "type";

    List<String> ALL_MIDDLEWARE_TYPE = CollectionUtils.newArrayList(PREFIX_DATASOURCE
                ,PREFIX_CACHE,PREFIX_REDIS,PREFIX_ELASTICSEARCH,PREFIX_KAFKA,PREFIX_SENTINEL,
                PREFIX_STORAGE,PREFIX_VMS,PREFIX_ZOOKEEPER,PREFIX_FLINK,PREFIX_HADOOP,
                PREFIX_YARN,PREFIX_HBASE,PREFIX_HIVE,PREFIX_BATCH,PREFIX_ROCKETMQ,
                PREFIX_PAY);
}
