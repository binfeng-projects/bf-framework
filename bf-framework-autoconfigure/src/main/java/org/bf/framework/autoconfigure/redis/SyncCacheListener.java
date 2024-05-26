package org.bf.framework.autoconfigure.redis;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.cache.sync.CacheSync;
import org.bf.framework.boot.support.cache.sync.SyncCacheProperties;
import org.bf.framework.common.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class SyncCacheListener implements RedisTopicListener,CacheSync {
    public static final String TOPIC_SYNC_CACHE = "sync_cache";
    private StringRedisTemplate redisTemplate;
    public SyncCacheListener(StringRedisTemplate redisTemplate) {
        if(redisTemplate == null) {
            throw new RuntimeException("redisTemplate cannot be null");
        }
        this.redisTemplate = redisTemplate;
    }
    @Override
    public void onRedisTopic(String message, String topicName) {
        if(StringUtils.isBlank(message) || StringUtils.isBlank(topicName)){
            return;
        }
        if(!TOPIC_SYNC_CACHE.equals(topicName)){
            return;
        }
        SyncCacheProperties msg = JSON.parseObject(message, SyncCacheProperties.class);
        if(msg == null) {
            return;
        }
        CacheSync.super.syncCache(msg);
    }

    @Override
    public boolean syncCache(SyncCacheProperties properties) {
        redisTemplate.convertAndSend(TOPIC_SYNC_CACHE,properties);
        return true;
    }
}
