package org.bf.framework.autoconfigure.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bf.framework.boot.annotation.auto.EnableConfig;
import org.bf.framework.boot.annotation.auto.EnableConfigHandler;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.support.cache.CacheConfig;
import org.bf.framework.boot.support.cache.LocalCacheProperties;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.ssl.SslOptions;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.SSLParameters;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.bf.framework.autoconfigure.redis.RedisTopicListener.LISTENER_METHOD;
import static org.bf.framework.autoconfigure.redis.SyncCacheListener.TOPIC_SYNC_CACHE;
import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

@AutoConfiguration
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnMissingBean(value = RedisAutoConfig.class,name = CacheConfig.PRIMARY_REMOTE_CACHE_MANAGER)
@EnableConfig(RedisAutoConfig.class)
@Slf4j
public class RedisAutoConfig implements EnableConfigHandler<RedisProperties> {
    private static final String CLIENT_TYPE_DEFAULT = "lettuce";
    private static final String PREFIX = PREFIX_REDIS;

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public RedisProperties bindInstance(Map<String,Object> map) {
        return createConfig(map);
    }

    public static RedisProperties createConfig(Map<String,Object> map) {
        return new RedisProperties();
    }
    @Override
    public List<Middleware> processRegisterBean(Map<String, Object> map, BeanDefinitionRegistry registry) {
        String schema = YamlUtil.getConfigSchema(map);
        RedisProperties redisProperties = (RedisProperties)(YamlUtil.getConfigBind(map));
        List<Middleware> result = processRedisson(schema,redisProperties);
        //配置redisTemplate和cacheManager
        RedisConnectionFactory connFac = null;
        RedisConfiguration cfg = getRedisConfiguration(redisProperties);
        if(CLIENT_TYPE_DEFAULT.equals(redisProperties.getType())) {
            connFac = new LettuceConnectionFactory(cfg,getLettuceClientConfiguration(redisProperties));
        } else {
            connFac = createJedisConnectionFactory(redisProperties);
        }
        //StringRedisTemplate配置
        String prefixAndSchema = PREFIX + DOT + schema;
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(connFac);
        result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(StringRedisTemplate.class).setBean(stringRedisTemplate));

        //cacheManager配置
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connFac);
        //本地缓存配置了5分钟，一小时等不同时间的缓存策略，redis也遵循同样配置一份redis版本的策略
        YamlUtil.parsePrefix(PREFIX_CACHE, LocalCacheProperties::newConfig, configMap -> {
            LocalCacheProperties caffeineProperties = (LocalCacheProperties)YamlUtil.getConfigBind(configMap);
            String cacheSchema = YamlUtil.getConfigSchema(configMap);
            long ttl = caffeineProperties.getExpireAfterWrite();
            String finalCachePrefix = prefixAndSchema + DOT + cacheSchema + ":";
            RedisCacheConfiguration rcg = newRedisCacheManagerConfig(finalCachePrefix,ttl);
            if(YamlUtil.firstCallBack(configMap)) { //第一个配置作为default
                builder.cacheDefaults(rcg);
            }
            builder.withCacheConfiguration(cacheSchema,rcg);
        });
        RedisCacheManager cacheManager = builder.build();
        cacheManager.afterPropertiesSet();//一定要执行
        Middleware middlewareCm = new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(RedisCacheManager.class).setBean(cacheManager);
        result.add(middlewareCm);

        //redis topic监听器
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connFac);
        RedisProperties.TopicListener listener = redisProperties.getTopicListener();
        if(null != listener && CollectionUtils.isNotEmpty(listener.getTopics()) && StringUtils.isNotBlank(listener.getListener())) {
            try {
                Class<?> cls = Class.forName(listener.getListener());
                if (!RedisTopicListener.class.isAssignableFrom(cls)) {
                    throw new RuntimeException(listener.getListener() +  " not implement " + RedisTopicListener.class.getName());
                }
                MessageListenerAdapter adp = new MessageListenerAdapter(cls.getDeclaredConstructor().newInstance(),LISTENER_METHOD);
                List<PatternTopic> topics = listener.getTopics().stream().filter(s ->
                        StringUtils.isNotBlank(s) && !TOPIC_SYNC_CACHE.equals(s)
                ).map(PatternTopic::new).collect(Collectors.toList());
                container.addMessageListener(adp,topics);
            } catch (Exception ex) {
                log.error("add redis topic listener error,",ex);
                throw new RuntimeException("add redis topic listener error" + ex.getMessage());
            }
        }
        //添加默认的清理本地缓存的listener
        SyncCacheListener syncCacheListener = new SyncCacheListener(stringRedisTemplate);
        MessageListenerAdapter adp = new MessageListenerAdapter(syncCacheListener,LISTENER_METHOD);
        container.addMessageListener(adp,new PatternTopic(TOPIC_SYNC_CACHE));
        if(YamlUtil.firstCallBack(map)) {
            //注册remoteCacheManager,使primaryMultilevelCache生效
            SpringUtil.registrySingleton(CacheConfig.PRIMARY_REMOTE_CACHE_MANAGER,cacheManager);
            SpringUtil.registrySingleton(CacheConfig.PRIMARY_CACHE_SYNC,syncCacheListener);
        }
        return result;
    }
    //--------------------------redisson----------------------------------
    private static String redissonAddress(String host,int port){
        return "redis://" + host + ":" + port ;
    }
    public static List<Middleware> processRedisson(String schema,RedisProperties redisProperties) {
        List<Middleware> result = CollectionUtils.newArrayList();
        RedisConfiguration redisConfig = getRedisConfiguration(redisProperties);
        if(!redisProperties.isUseRedisson()) {
            return result;
        }
        //redission
        Config redissionConfig = new Config();
        if (redisConfig instanceof RedisClusterConfiguration) {
            RedisClusterConfiguration cluster =  (RedisClusterConfiguration) redisConfig;
            ClusterServersConfig csc = redissionConfig.useClusterServers().setUsername(cluster.getUsername())
                    .setPassword(new String(cluster.getPassword().get()));
            for (RedisNode node : cluster.getClusterNodes()){
                csc.addNodeAddress(redissonAddress(node.getHost(),node.getPort()));
            }
        }
        else if (redisConfig instanceof RedisSentinelConfiguration) {
            RedisSentinelConfiguration sentinel =  (RedisSentinelConfiguration) redisConfig;
            SentinelServersConfig ssc = redissionConfig.useSentinelServers().setUsername(sentinel.getUsername())
                    .setPassword(new String(sentinel.getPassword().get()))
                    .setSentinelPassword(new String(sentinel.getSentinelPassword().get()))
                    .setDatabase(sentinel.getDatabase()).setMasterName(sentinel.getMaster().getName());
            for (RedisNode node : sentinel.getSentinels()){
                ssc.addSentinelAddress(redissonAddress(node.getHost(),node.getPort()));
            }
        }else{
            RedisStandaloneConfiguration stand =  (RedisStandaloneConfiguration) redisConfig;
            redissionConfig.useSingleServer().setAddress(redissonAddress(stand.getHostName(),stand.getPort()))
                    .setUsername(stand.getUsername()).setPassword(new String(stand.getPassword().get()))
                    .setDatabase(stand.getDatabase()).setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle())
                    .setConnectionPoolSize(redisProperties.getPool().getMaxActive());
        }
        RedissonClient redissonClient = Redisson.create(redissionConfig);
        result.add(new Middleware().setPrefix(PREFIX).setSchemaName(schema).setType(RedissonClient.class).setBean(redissonClient));
        return result;
    }
    public static RedisCacheConfiguration newRedisCacheManagerConfig(String prefix,long second) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        config = config.entryTtl(Duration.ofSeconds(second));
        config = config.prefixCacheNameWith(prefix);
        return config;
    }

    //--------------------------standalone,cluster,sentinel commonConfig----------------------------------
    public static RedisStandaloneConfiguration getStandaloneConfig(RedisProperties properties) {
        if(StringUtils.isBlank(properties.getUrl()) && StringUtils.isBlank(properties.getHost())) {
            throw new RuntimeException("redis url , host cannot be both null");
        }
        String host = properties.getHost();
        int port = properties.getPort();
        if(StringUtils.isNotBlank(properties.getUrl())) {
            String[] hostAndPort = properties.getUrl().split(":");
            host = hostAndPort[0];
            properties.setHost(host);
            if(hostAndPort.length > 1) {
                port = Integer.parseInt(hostAndPort[1]);
                properties.setPort(port);
            }
        }
        properties.setUrl(host + ":" + port); //塞回去,可能后面会用到
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getHost());
        config.setPort(properties.getPort());
        config.setUsername(properties.getUsername());
        config.setPassword(RedisPassword.of(properties.getPassword()));
        config.setDatabase(properties.getDatabase());
        return config;
    }

    public static RedisSentinelConfiguration getSentinelConfig(RedisProperties properties) {
        org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel sentinel = properties.getSentinel();
        if (sentinel != null && CollectionUtils.isNotEmpty(sentinel.getNodes())) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(properties.getSentinel().getMaster());
            List<RedisNode> nodes = new ArrayList<>();
            for (String node : sentinel.getNodes()) {
                String[] components = node.split(":");
                nodes.add(new RedisNode(components[0],Integer.parseInt(components[1])));
            }
            config.setSentinels(nodes);
            config.setUsername(properties.getUsername());
            String password = properties.getPassword();
            if (password != null) {
                config.setPassword(RedisPassword.of(password));
            }
            config.setSentinelUsername(properties.getSentinel().getUsername());
            String sentinelPassword = properties.getSentinel().getPassword();
            if (sentinelPassword != null) {
                config.setSentinelPassword(RedisPassword.of(sentinelPassword));
            }
            config.setDatabase(properties.getDatabase());
            return config;
        }
        return null;
    }
    protected static RedisClusterConfiguration getClusterConfiguration(RedisProperties properties) {
        org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster clusterProperties = properties.getCluster();
        if (clusterProperties != null && CollectionUtils.isNotEmpty(clusterProperties.getNodes())) {
            RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
            if (clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }
            config.setUsername(properties.getUsername());
            String password = properties.getPassword();
            if (password != null) {
                config.setPassword(RedisPassword.of(password));
            }
            return config;
        }
        return null;
    }
    //------------------------------------------- LettuceClientConfiguration-------------------------------
    private static RedisConfiguration getRedisConfiguration(RedisProperties properties) {
        RedisConfiguration cfg = getSentinelConfig(properties);
        if (cfg == null) {
            cfg = getClusterConfiguration(properties);
        }
        if (cfg == null) {
            cfg = getStandaloneConfig(properties);
        }
        return cfg;
    }
    private LettuceClientConfiguration getLettuceClientConfiguration(RedisProperties properties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = PoolBuilderFactory.createBuilder(properties.getPool());
        if (properties.getTimeout() != null) {
            builder.commandTimeout(properties.getTimeout());
        }
        if (properties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = properties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(lettuce.getShutdownTimeout());
            }
        }
        builder.clientOptions(createClientOptions(properties));
        builder.clientResources(DefaultClientResources.builder().build());
        return builder.build();
    }
    private ClientOptions createClientOptions(RedisProperties properties) {
        ClientOptions.Builder builder = initializeClientOptionsBuilder(properties);
        Duration connectTimeout = properties.getConnectTimeout();
        if (connectTimeout != null) {
            builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
        }
        builder.timeoutOptions(TimeoutOptions.enabled());
        SslBundle sslBundle = useSSL(properties);
        if (sslBundle == null) {
            return builder.build();
        }
        io.lettuce.core.SslOptions.Builder sslOptionsBuilder = io.lettuce.core.SslOptions.builder();
        sslOptionsBuilder.keyManager(sslBundle.getManagers().getKeyManagerFactory());
        sslOptionsBuilder.trustManager(sslBundle.getManagers().getTrustManagerFactory());
        SslOptions sslOptions = sslBundle.getOptions();
        if (sslOptions.getCiphers() != null) {
            sslOptionsBuilder.cipherSuites(sslOptions.getCiphers());
        }
        if (sslOptions.getEnabledProtocols() != null) {
            sslOptionsBuilder.protocols(sslOptions.getEnabledProtocols());
        }
        return builder.sslOptions(sslOptionsBuilder.build()).build();
    }

    private static ClientOptions.Builder initializeClientOptionsBuilder(RedisProperties properties) {
        if (properties.getLettuce() != null) {
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
            RedisProperties.Lettuce refreshProperties = properties.getLettuce();
            ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
                    .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
            if (refreshProperties.getPeriod() != null) {
                refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
            }
            if (refreshProperties.isAdaptive()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            return builder.topologyRefreshOptions(refreshBuilder.build());
        }
        return ClientOptions.builder();
    }
    private static class PoolBuilderFactory {
        static LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool pool) {
            return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(pool));
        }
        private static GenericObjectPoolConfig<?> getPoolConfig(org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool properties) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(properties.getTimeBetweenEvictionRuns());
            }
            if (properties.getMaxWait() != null) {
                config.setMaxWait(properties.getMaxWait());
            }
            return config;
        }
    }
    public SslBundle useSSL(RedisProperties properties){
        org.springframework.boot.autoconfigure.data.redis.RedisProperties.Ssl ssl = properties.getSsl();
        if (ssl== null || !ssl.isEnabled()) {
            return null;
        }
        return getBundle(ssl.getBundle());
    }
    //------------------------------------------- JedisClientConfiguration-------------------------------
    private JedisConnectionFactory createJedisConnectionFactory(RedisProperties properties) {
        JedisClientConfiguration clientConfiguration = getJedisClientConfiguration(properties);
        RedisSentinelConfiguration sentinelConfiguration = getSentinelConfig(properties);
        if (sentinelConfiguration != null) {
            return new JedisConnectionFactory(sentinelConfiguration, clientConfiguration);
        }
        RedisClusterConfiguration clusterConfiguration = getClusterConfiguration(properties);
        if (clusterConfiguration != null) {
            return new JedisConnectionFactory(clusterConfiguration, clientConfiguration);
        }
        return new JedisConnectionFactory(getStandaloneConfig(properties), clientConfiguration);
    }

    private JedisClientConfiguration getJedisClientConfiguration(RedisProperties properties) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(properties);
        builder.usePooling().poolConfig(jedisPoolConfig(properties.getPool()));
        SslBundle sslBundle = useSSL(properties);
        if (sslBundle != null) {
            JedisClientConfiguration.JedisSslClientConfigurationBuilder sslBuilder = builder.useSsl();
            sslBuilder.sslSocketFactory(sslBundle.createSslContext().getSocketFactory());
            SslOptions sslOptions = sslBundle.getOptions();
            SSLParameters sslParameters = new SSLParameters();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            map.from(sslOptions.getCiphers()).to(sslParameters::setCipherSuites);
            map.from(sslOptions.getEnabledProtocols()).to(sslParameters::setProtocols);
            sslBuilder.sslParameters(sslParameters);
        }
        return builder.build();
    }

    private static JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(RedisProperties properties) {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(properties.getTimeout()).to(builder::readTimeout);
        map.from(properties.getConnectTimeout()).to(builder::connectTimeout);
//        map.from(properties.getClientName()).whenHasText().to(builder::clientName);
        return builder;
    }
    private static JedisPoolConfig jedisPoolConfig(org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getTimeBetweenEvictionRuns() != null) {
            config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
        }
        if (pool.getMaxWait() != null) {
            config.setMaxWait(pool.getMaxWait());
        }
        return config;
    }
}
