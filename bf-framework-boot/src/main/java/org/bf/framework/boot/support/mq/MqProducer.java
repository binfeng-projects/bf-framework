package org.bf.framework.boot.support.mq;

/**
 */
public interface MqProducer {
    default void send(String topic, String data) {
        this.send(topic,null,data);
    }

    default void send(String topic, String key, String data) {
        send(topic,null,key,data);
    }

    void send(String topic, Integer partition, String key, String data);
}

