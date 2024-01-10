package org.bf.framework.boot.constant;

import static org.bf.framework.boot.constant.FrameworkConst.*;

/**
 * 中间件用到的一些常量
 */
public interface MiddlewareConst {
    //-----------------------------------datasource-------------------------------
    String PREFIX_DATASOURCE = BF + DOT + "datasource";
    String BEAN_DATASOURCE = "_DataSource";
    String BEAN_PLATFORMTRANSACTIONMANAGER = "_PlatformTransactionManager";
    //-----------------------------------jooq------------------------------
    String PREFIX_JOOQ = BF + DOT + "jooq";
    String BEAN_DSLCONTEXT = "_DSLContext";
    //-----------------------------------mybatis------------------------------
    String PREFIX_MYBATIS = BF + DOT + "mybatis";
    String BEAN_SQLSESSIONFACTORY = "_SqlSessionFactory";

    //-----------------------------------local-cache------------------------------
    String PREFIX_CACHE = BF + DOT + "cache";
    String SCHEMA_CACHE_LOCAL = "local";
    //----------------------------------- redis-----------------------------------
    String PREFIX_REDIS = BF + DOT + "redis";
    String BEAN_STRINGREDISTEMPLATE = "_StringRedisTemplate";
    String BEAN_REDISCACHEMANAGER = "_RedisCacheManager";
    String BEAN_REDISSONCLIENT = "_RedissonClient";
    String BEAN_CACHEMANAGER = "_CacheManager";
    //----------------------------------- kafka-----------------------------------
    String PREFIX_KAFKA = BF + DOT + "kafka";
    String BEAN_KAFKATEMPLATE = "_KafkaTemplate";
    String BEAN_KAFKALISTENERCONTAINERFACTORY = "_KafkaListenerContainerFactory";
    //----------------------------------- elasticsearch-----------------------------------
    String PREFIX_ELASTICSEARCH = BF + DOT + "elasticsearch";
    String BEAN_ELASTICSEARCHTEMPLATE = "_ElasticsearchTemplate";
//    String DEFAULT_SEARCH_ANALYZER = "ik_max_word";
    //----------------------------------- xxl-----------------------------------
    String PREFIX_XXL = BF + DOT + "xxl";
    String BEAN_XXLJOBEXECUTOR = "_XxlJobExecutor";
    //----------------------------------- apollo -----------------------------------
    String PREFIX_APOLLO = BF + DOT + "apollo";
    //----------------------------------- cloud platform -----------------------------------
    String CLOUD_PLATFORM_ALIYUN = "aliyun";
    String CLOUD_PLATFORM_AWS = "aws";
    String CLOUD_PLATFORM_TENCENT = "tencent";
    //----------------------------------- storage -----------------------------------
    String PREFIX_STORAGE = BF + DOT + "storage";
    String BEAN_STORAGEPROXY = "_StorageProxy";
    //----------------------------------- vms -----------------------------------
    String PREFIX_VMS = BF + DOT + "vms";
    String BEAN_VMSPROXY = "_VmsProxy";
    //----------------------------------- zookeeper -----------------------------------
    String PREFIX_ZOOKEEPER = BF + DOT + "zookeeper";
    String BEAN_CURATORFRAMEWORK = "_CuratorFramework";
    //----------------------------------- dubbo -----------------------------------
    String PREFIX_DUBBO = BF + DOT + "dubbo";
    //----------------------------------- flink -----------------------------------
    String PREFIX_FLINK = BF + DOT + "flink";
    String BEAN_KAFKASOURCEBUILDER = "_KafkaSourceBuilder";
    String BEAN_KAFKASINKBUILDER = "_KafkaSinkBuilder";
    //----------------------------------- hadoop -----------------------------------
    String PREFIX_HADOOP = BF + DOT + "hadoop";
    String BEAN_HADOOPPROXY = "_HadoopProxy";
//    String BEAN_FSSHELL = "_FsShell";
//----------------------------------- hbase -----------------------------------
    String PREFIX_HBASE = BF + DOT + "hbase";
    String BEAN_HBASEPROXY = "_HbaseProxy";
//----------------------------------- hive -----------------------------------
    String PREFIX_HIVE = BF + DOT + "hive";
    String BEAN_HIVETEMPLATE = "_HiveTemplate";
//----------------------------------- yarn -----------------------------------
    String PREFIX_YARN = BF + DOT + "yarn";
    String BEAN_YARNPROXY = "_YarnProxy";
    String BEAN_YARNCONTAINER = "_YarnContainer";
    //----------------------------------- batch -----------------------------------
    String PREFIX_BATCH = BF + DOT + "batch";
    String BEAN_BATCHPROXY = "_BatchProxy";
    //----------------------------------- 通用 -----------------------------------
    String ENABLED = "enabled";
    String URL = "url";
    String TYPE = "type";
}
