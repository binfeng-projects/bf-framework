package org.bf.framework.autoconfigure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnMissingBean(value = KafkaAutoConfig.class)
@ConditionalOnProperty(prefix = PREFIX_KAFKA, value = ENABLED, matchIfMissing = true)
@EnableConfig(KafkaAutoConfig.class)
@Slf4j
public class KafkaAutoConfig implements EnableConfigHandler<KafkaProperties> {
    private static final String PREFIX = PREFIX_KAFKA;
    @Override
    public String getPrefix() {
        return PREFIX;
    }
    @Override
    public KafkaProperties bindInstance(Map<String, Object> properties) {
        return new KafkaProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> map, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            KafkaProperties cfg = (KafkaProperties)YamlUtil.getConfigBind(map);
            //---------------------------producer & template --------------------------
//            Map<String, Object> producerProperties = cfg.buildProducerProperties(SpringUtil.getBean(SslBundles.class));
            Map<String, Object> producerProperties = cfg.buildProducerProperties();
            producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getBootstrapServers());
//            producerProperties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
            DefaultKafkaProducerFactory<Object, Object> kafkaProducerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
            String transactionIdPrefix = cfg.getProducer().getTransactionIdPrefix();
            if (StringUtils.isNotBlank(transactionIdPrefix)) {
                kafkaProducerFactory.setTransactionIdPrefix(transactionIdPrefix);
//                KafkaTransactionManager<Object, Object> producerTransactionManager = new KafkaTransactionManager<>(kafkaProducerFactory);
            }
            KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<Object, Object>(kafkaProducerFactory);
//            messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
            //默认newc出来的template就已经设置了listener
//            map.from(defaultProductListener).to(kafkaTemplate::setProducerListener);
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(KafkaTemplate.class).setBean(kafkaTemplate));

            //--------------------------- Consumer--------------------------
//            Map<String, Object> consumerProperties = cfg.buildConsumerProperties(SpringUtil.getBean(SslBundles.class));
            Map<String, Object> consumerProperties = cfg.buildConsumerProperties();
            consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getBootstrapServers());
            DefaultKafkaConsumerFactory<Object, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
            ConcurrentKafkaListenerContainerFactory<Object, Object> listenerFactory = new ConcurrentKafkaListenerContainerFactory<>();
            listenerFactory.setConsumerFactory(consumerFactory);
            configureListenerFactory(listenerFactory,cfg);
            configureContainer(listenerFactory.getContainerProperties(),cfg);
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(KafkaListenerContainerFactory.class).setBean(listenerFactory));
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
    private void configureListenerFactory(ConcurrentKafkaListenerContainerFactory<Object, Object> factory,KafkaProperties cfg) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        KafkaProperties.Listener properties = cfg.getListener();
        if (properties.getType().equals(KafkaProperties.Listener.Type.BATCH)) {
            factory.setBatchListener(true);
        }
        map.from(properties::getConcurrency).to(factory::setConcurrency);
//        map.from(properties::isAutoStartup).to(factory::setAutoStartup);
//        map.from(properties::getChangeConsumerThreadName).to(factory::setChangeConsumerThreadName);
//        map.from(this.batchMessageConverter).to(factory::setBatchMessageConverter);
//        map.from(this.recordMessageConverter).to(factory::setRecordMessageConverter);
//        map.from(this.recordFilterStrategy).to(factory::setRecordFilterStrategy);
//        map.from(this.replyTemplate).to(factory::setReplyTemplate);
//        map.from(this.commonErrorHandler).to(factory::setCommonErrorHandler);
//        map.from(this.afterRollbackProcessor).to(factory::setAfterRollbackProcessor);
//        map.from(this.recordInterceptor).to(factory::setRecordInterceptor);
//        map.from(this.batchInterceptor).to(factory::setBatchInterceptor);
//        map.from(this.threadNameSupplier).to(factory::setThreadNameSupplier);
    }

    private void configureContainer(ContainerProperties container,KafkaProperties cfg) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        KafkaProperties.Listener properties = cfg.getListener();
        map.from(properties::getAckMode).to(container::setAckMode);
//        map.from(properties::getAsyncAcks).to(container::setAsyncAcks);
        map.from(properties::getClientId).to(container::setClientId);
        map.from(properties::getAckCount).to(container::setAckCount);
        map.from(properties::getAckTime).as(Duration::toMillis).to(container::setAckTime);
        map.from(properties::getPollTimeout).as(Duration::toMillis).to(container::setPollTimeout);
        map.from(properties::getNoPollThreshold).to(container::setNoPollThreshold);
        map.from(properties.getIdleBetweenPolls()).as(Duration::toMillis).to(container::setIdleBetweenPolls);
        map.from(properties::getIdleEventInterval).as(Duration::toMillis).to(container::setIdleEventInterval);
        map.from(properties::getIdlePartitionEventInterval)
                .as(Duration::toMillis)
                .to(container::setIdlePartitionEventInterval);
        map.from(properties::getMonitorInterval)
                .as(Duration::getSeconds)
                .as(Number::intValue)
                .to(container::setMonitorInterval);
        map.from(properties::getLogContainerConfig).to(container::setLogContainerConfig);
        map.from(properties::isMissingTopicsFatal).to(container::setMissingTopicsFatal);
        map.from(properties::isImmediateStop).to(container::setStopImmediate);
//        map.from(this.transactionManager).to(container::setTransactionManager);
//        map.from(this.rebalanceListener).to(container::setConsumerRebalanceListener);
//        map.from(this.listenerTaskExecutor).to(container::setListenerTaskExecutor);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableKafka
//    @ConditionalOnMissingBean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    static class EnableKafkaConfiguration {
    }
}
