package org.bf.framework.autoconfigure.redis;

public interface RedisTopicListener {
    String LISTENER_METHOD = "onRedisTopic";
    void onRedisTopic(String message,String topicName);
}
