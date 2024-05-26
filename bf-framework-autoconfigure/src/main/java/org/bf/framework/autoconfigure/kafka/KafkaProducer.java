package org.bf.framework.autoconfigure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.mq.MqProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class KafkaProducer implements MqProducer<SendResult<Object,Object>> {
    KafkaTemplate<Object,Object> template;

    public KafkaProducer(KafkaTemplate<Object,Object> template){
        this.template = template;
    }
    @Override
    public CompletableFuture<SendResult<Object,Object>> asyncSend(String topic, Object data) {
        return template.send(topic,data);
    }

    @Override
    public SendResult<Object,Object> syncSend(String topic, Object data) {
        try {
            return asyncSend(topic,data).get();
        } catch (Exception e){
            log.error("kafka sync send error topic{}, data{}",topic,data);
            return null;
        }
    }
}
