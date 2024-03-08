package org.bf.framework.autoconfigure.flink;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;

import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

/**
 * 配置flink的source和sink
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = FlinkAutoConfig.PREFIX, name = ENABLED)
@ConditionalOnMissingBean(value = FlinkAutoConfig.class)
@EnableConfig(FlinkAutoConfig.class)
@Slf4j
public class FlinkAutoConfig implements EnableConfigHandler<FlinkProperties> {
    public static final String PREFIX = PREFIX_FLINK;
    public static final String TYPE_KAFKA = "kafka";
    public static final String TYPE_PULSAR = "pulsar";
    public static final String TYPE_RABBITMQ = "rabbitmq";
    public static final String TYPE_MONGODB = "mongodb";
    @Override
    public String getPrefix() {
        return PREFIX;
    }
    @Override
    public FlinkProperties bindInstance(Map<String, Object> properties) {
        return new FlinkProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> properties, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(properties);
        List<Middleware> result = null;
        try {
            //不注册bean，只注册key
            FlinkProperties cfg = (FlinkProperties)YamlUtil.getConfigBind(properties);
            if(TYPE_KAFKA.equals(schema)) {
                result = configKafka(cfg);
            }
            else if(TYPE_PULSAR.equals(schema)) {
                result = configPulsar(cfg);
            }
            else if(TYPE_RABBITMQ.equals(schema)) {
                result = configRabbitmq(cfg);
            }
            else if(TYPE_MONGODB.equals(schema)) {
                result = configMongodb(cfg);
            }
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }
    public List<Middleware> configKafka(FlinkProperties p){
        //暂时没用
        List<Middleware> result = CollectionUtils.newArrayList();
        //和传统的schema有点不一样，相当于有两层级的key一起做schema，例如kafka.default
        List<Middleware> middlewareList = CollectionUtils.newArrayList();
        if(CollectionUtils.isNotEmpty(p.getSourceSet())) {
            for (String schema: p.getSourceSet()) {
                KafkaProperties cfg = YamlUtil.configBind(PREFIX_KAFKA,schema,new KafkaProperties());
                KafkaSourceBuilder<String> sourceBuilder = KafkaSource.<String>builder()
                        .setValueOnlyDeserializer(new SimpleStringSchema());
                //默认从最早开始，可以自定义
                // 从消费组提交的位点开始消费，不指定位点重置策略
//                sourceBuilder.setStartingOffsets(OffsetsInitializer.committedOffsets())
//                        // 从消费组提交的位点开始消费，如果提交位点不存在，使用最早位点
//                        .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
//                        // 从时间戳大于等于指定时间戳（毫秒）的数据开始消费
//                        .setStartingOffsets(OffsetsInitializer.timestamp(1657256176000L))
//                        // 从最早位点开始消费
//                        .setStartingOffsets(OffsetsInitializer.earliest())
//                        // 从最末尾位点开始消费
//                        .setStartingOffsets(OffsetsInitializer.latest());
                sourceBuilder.setStartingOffsets(OffsetsInitializer.earliest());
                Map<String, Object> consumerProps = cfg.buildConsumerProperties(SpringUtil.getBean(SslBundles.class));
                if(MapUtils.isNotEmpty(consumerProps)) {
                    consumerProps.forEach((k,v) -> sourceBuilder.setProperty(k,String.valueOf(v)));
                }
                sourceBuilder.setBootstrapServers(StringUtils.join(",",cfg.getBootstrapServers()));
                sourceBuilder.setGroupId(cfg.getConsumer().getGroupId());
//                动态分区检查
//                sourceBuilder.setProperty("partition.discovery.interval.ms", "10000"); // 每 10 秒检查一次新分区
                String typeSchema = TYPE_KAFKA + DOT + schema;
                Middleware middleware = new Middleware().setPrefix(PREFIX).setSchemaName(typeSchema).setType(KafkaSourceBuilder.class).setBean(sourceBuilder);
                Middleware.register(PREFIX + DOT + typeSchema,middlewareList);
                middlewareList.add(middleware);
            }
        }
        if(CollectionUtils.isNotEmpty(p.getSinkSet())) {
            for (String schema: p.getSinkSet()) {
                String typeSchema = TYPE_KAFKA + DOT + schema;
                KafkaProperties cfg = YamlUtil.configBind(PREFIX_KAFKA,schema,new KafkaProperties());
                KafkaSinkBuilder<String> sinkBuilder = KafkaSink.<String>builder()
                        //涉及到topic，留给业务自定义
//                        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
//                                .setTopic("topic-name")
//                                .setValueSerializationSchema(new SimpleStringSchema())
//                                .setKeySerializationSchema(new SimpleStringSchema())
//                                .build()
//                        )
                        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE);
                Map<String, Object> producerProps = cfg.buildProducerProperties(SpringUtil.getBean(SslBundles.class));
                if(MapUtils.isNotEmpty(producerProps)) {
                    producerProps.forEach((k,v) -> sinkBuilder.setProperty(k,String.valueOf(v)));
                }
                sinkBuilder.setBootstrapServers(StringUtils.join(",",cfg.getBootstrapServers()));
                Middleware middleware = new Middleware().setPrefix(PREFIX).setSchemaName(typeSchema).setType(KafkaSinkBuilder.class).setBean(sinkBuilder);
                Middleware.register(PREFIX + DOT + typeSchema,middlewareList);
                middlewareList.add(middleware);
            }
        }
        result.addAll(middlewareList);
        return result;
    }
    public List<Middleware> configPulsar(FlinkProperties p){
        List<Middleware> result = CollectionUtils.newArrayList();
        return result;
    }
    public List<Middleware> configRabbitmq(FlinkProperties p){
        List<Middleware> result = CollectionUtils.newArrayList();
        return result;
    }
    public List<Middleware> configMongodb(FlinkProperties p){
        List<Middleware> result = CollectionUtils.newArrayList();
        return result;
    }
}
