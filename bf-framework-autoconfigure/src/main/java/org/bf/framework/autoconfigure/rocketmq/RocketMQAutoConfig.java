package org.bf.framework.autoconfigure.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.autoconfigure.*;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.constant.FrameworkConst;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.MiddlewareConst.ENABLED;
import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_ROCKETMQ;

@AutoConfiguration
@ConditionalOnClass({RocketMQTemplate.class,MQAdmin.class})
@ConditionalOnMissingBean(value = RocketMQAutoConfig.class)
@EnableConfig(RocketMQAutoConfig.class)
@Import({ListenerContainerConfiguration.class, ExtProducerResetConfiguration.class,
        ExtConsumerResetConfiguration.class, RocketMQTransactionConfiguration.class, RocketMQListenerConfiguration.class})
@ConditionalOnProperty(prefix = PREFIX_ROCKETMQ, value = ENABLED, matchIfMissing = true)
@AutoConfigureBefore({RocketMQTransactionConfiguration.class,RocketMQListenerConfiguration.class})
//@AutoConfigureAfter({MessageConverterConfiguration.class})
@Slf4j
public class RocketMQAutoConfig implements EnableConfigHandler<RocketMQProperties> {
    private static final String PREFIX = PREFIX_ROCKETMQ;
    private RocketMQMessageConverter rocketMQMessageConverter;
    @Override
    public String getPrefix() {
        return PREFIX;
    }
    @Override
    public RocketMQProperties bindInstance(Map<String, Object> properties) {
        return new RocketMQProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String,Object> map, BeanDefinitionRegistry r) {
        String schema = YamlUtil.getConfigSchema(map);
        List<Middleware> result = CollectionUtils.newArrayList();
        try {
            RocketMQProperties cfg = (RocketMQProperties)YamlUtil.getConfigBind(map);
            RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
            rocketMQTemplate.setProducer(createProducer(cfg));
            rocketMQTemplate.setConsumer(createLitePullConsumer(cfg));
            if(YamlUtil.firstCallBack(map)) { //第一个配置作为default,生成rocketmq client需要的bean
                SpringUtil.registrySingleton("rocketMQProperties",cfg);
                rocketMQMessageConverter = new RocketMQMessageConverter();
                SpringUtil.registrySingleton("rocketMQMessageConverter",rocketMQMessageConverter);
                SpringUtil.registrySingleton(RocketMQAutoConfiguration.PRODUCER_BEAN_NAME,rocketMQTemplate.getProducer());
                if(rocketMQTemplate.getConsumer() != null) {
                    SpringUtil.registrySingleton(RocketMQAutoConfiguration.CONSUMER_BEAN_NAME,rocketMQTemplate.getConsumer());
                }
                SpringUtil.registrySingleton(RocketMQAutoConfiguration.ROCKETMQ_TEMPLATE_DEFAULT_GLOBAL_NAME,rocketMQTemplate);
                // some env key
                fillRocketDefaultConfig(cfg);
            }
            rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
            rocketMQTemplate.afterPropertiesSet();
            result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(RocketMQTemplate.class).setBean(rocketMQTemplate));
        } catch (Exception e) {
            log.error("processRegisterBean error",e);
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public void fillRocketDefaultConfig(RocketMQProperties cfg){
        Map<String,Object> frameworkCfg = (Map<String,Object>)SpringUtil.getEnvironment().getPropertySources().get(FrameworkConst.FRAMEWORK_KEY).getSource();
        RocketMQProperties.Producer producerConfig = cfg.getProducer();
        frameworkCfg.put("rocketmq.name-server",cfg.getNameServer());
        frameworkCfg.put("rocketmq.access-channel",cfg.getAccessChannel());
        //producer
        frameworkCfg.put("rocketmq.producer.group",producerConfig.getGroup());
        frameworkCfg.put("rocketmq.producer.accessKey",producerConfig.getAccessKey());
        frameworkCfg.put("rocketmq.producer.secretKey",producerConfig.getSecretKey());
        frameworkCfg.put("rocketmq.producer.customized-trace-topic",producerConfig.getCustomizedTraceTopic());
        //pull-consumer
        RocketMQProperties.PullConsumer pullConsumerConfig = cfg.getPullConsumer();
        String topicName = pullConsumerConfig.getTopic();
        if(StringUtils.hasLength(topicName)) {
            frameworkCfg.put("rocketmq.pull-consumer.group",pullConsumerConfig.getGroup());
            frameworkCfg.put("rocketmq.pull-consumer.topic",topicName);
            frameworkCfg.put("rocketmq.pull-consumer.access-key",pullConsumerConfig.getAccessKey());
            frameworkCfg.put("rocketmq.pull-consumer.secret-key",pullConsumerConfig.getSecretKey());
            frameworkCfg.put("rocketmq.pull-consumer.customized-trace-topic",pullConsumerConfig.getCustomizedTraceTopic());
        }
        //push consumer
        RocketMQProperties.PushConsumer pushConsumer = cfg.getConsumer();
        frameworkCfg.put("rocketmq.consumer.group",pushConsumer.getGroup());
        frameworkCfg.put("rocketmq.consumer.access-key",pushConsumer.getAccessKey());
        frameworkCfg.put("rocketmq.consumer.secret-key",pushConsumer.getSecretKey());
        frameworkCfg.put("rocketmq.consumer.customized-trace-topic",pushConsumer.getCustomizedTraceTopic());
    }
    public DefaultMQProducer createProducer(RocketMQProperties rocketMQProperties) {
        RocketMQProperties.Producer producerConfig = rocketMQProperties.getProducer();
        String nameServer = rocketMQProperties.getNameServer();
        String groupName = producerConfig.getGroup();
        Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
        Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");

        String accessChannel = rocketMQProperties.getAccessChannel();

        String ak = rocketMQProperties.getProducer().getAccessKey();
        String sk = rocketMQProperties.getProducer().getSecretKey();
        boolean isEnableMsgTrace = rocketMQProperties.getProducer().isEnableMsgTrace();
        String customizedTraceTopic = rocketMQProperties.getProducer().getCustomizedTraceTopic();

        DefaultMQProducer producer = RocketMQUtil.createDefaultMQProducer(groupName, ak, sk, isEnableMsgTrace, customizedTraceTopic);

        producer.setNamesrvAddr(nameServer);
        if (StringUtils.hasLength(accessChannel)) {
            producer.setAccessChannel(AccessChannel.valueOf(accessChannel));
        }
        producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());
        producer.setUseTLS(producerConfig.isTlsEnable());
        producer.setNamespace(producerConfig.getNamespace());
        producer.setInstanceName(producerConfig.getInstanceName());
        log.info(String.format("a producer (%s) init on namesrv %s",  groupName,nameServer));
        return producer;
    }

    public DefaultLitePullConsumer createLitePullConsumer(RocketMQProperties rocketMQProperties)
            throws MQClientException {
        RocketMQProperties.PullConsumer consumerConfig = rocketMQProperties.getPullConsumer();
        String nameServer = rocketMQProperties.getNameServer();
        String groupName = consumerConfig.getGroup();
        String topicName = consumerConfig.getTopic();
        Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
        Assert.hasText(groupName, "[rocketmq.pull-consumer.group] must not be null");
        if(!StringUtils.hasLength(topicName)) {
            return null;
        }
        String accessChannel = rocketMQProperties.getAccessChannel();
        MessageModel messageModel = MessageModel.valueOf(consumerConfig.getMessageModel());
        SelectorType selectorType = SelectorType.valueOf(consumerConfig.getSelectorType());
        String selectorExpression = consumerConfig.getSelectorExpression();
        String ak = consumerConfig.getAccessKey();
        String sk = consumerConfig.getSecretKey();
        int pullBatchSize = consumerConfig.getPullBatchSize();
        boolean useTLS = consumerConfig.isTlsEnable();
        DefaultLitePullConsumer litePullConsumer = RocketMQUtil.createDefaultLitePullConsumer(nameServer, accessChannel,
                groupName, topicName, messageModel, selectorType, selectorExpression, ak, sk, pullBatchSize, useTLS);
        litePullConsumer.setEnableMsgTrace(consumerConfig.isEnableMsgTrace());
        litePullConsumer.setCustomizedTraceTopic(consumerConfig.getCustomizedTraceTopic());
        litePullConsumer.setNamespace(consumerConfig.getNamespace());
        litePullConsumer.setInstanceName(consumerConfig.getInstanceName());
        log.info(String.format("a pull consumer(%s sub %s) init on namesrv %s",  groupName, topicName,nameServer));
        return litePullConsumer;
    }
}
