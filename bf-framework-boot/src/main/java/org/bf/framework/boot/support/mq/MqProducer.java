package org.bf.framework.boot.support.mq;

import java.util.concurrent.CompletableFuture;

/**
 */
public interface MqProducer<T> {
    CompletableFuture<T> asyncSend(String topic, Object data);
    T syncSend(String topic,Object data);
}

