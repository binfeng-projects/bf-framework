package org.bf.framework.autoconfigure.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bf.framework.boot.support.mq.MqProducer;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class RocketMQProducer implements MqProducer<SendResult> {
    RocketMQTemplate template;

    public RocketMQProducer(RocketMQTemplate template){
        this.template = template;
    }
    @Override
    public CompletableFuture<SendResult> asyncSend(String topic, Object data) {
        CompletableFuture<SendResult> completable = new CompletableFuture<SendResult>();
        template.asyncSend(topic, data, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                completable.complete(sendResult);
            }
            @Override
            public void onException(Throwable e) {
                completable.completeExceptionally(e);
            }
        });
        return completable;
    }
    @Override
    public SendResult syncSend(String topic, Object data) {
        try {
            return asyncSend(topic,data).get();
        } catch (Exception e){
            log.error("rocketmq sync send error topic{}, data{}",topic,data);
            return null;
        }
    }
}
