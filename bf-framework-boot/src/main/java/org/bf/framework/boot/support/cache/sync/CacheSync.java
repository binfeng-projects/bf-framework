package org.bf.framework.boot.support.cache.sync;

import org.bf.framework.boot.support.cache.sync.SyncCacheProperties;
import org.bf.framework.boot.util.CacheUitl;
public interface CacheSync {
    default boolean syncCache(SyncCacheProperties properties) {
        return CacheUitl.syncCache(properties);
    }
}
