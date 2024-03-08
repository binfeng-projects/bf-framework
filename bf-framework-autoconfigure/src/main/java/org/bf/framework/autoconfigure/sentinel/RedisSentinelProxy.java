package org.bf.framework.autoconfigure.sentinel;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.redis.RedisAutoConfig;
import org.bf.framework.autoconfigure.redis.RedisProperties;
import org.bf.framework.boot.util.YamlUtil;
import org.springframework.data.redis.connection.*;

import java.util.List;

import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_REDIS;

@Slf4j
public class RedisSentinelProxy extends SentinelProxy{

    public RedisSentinelProxy(SentinelProperties cfg) {
        super(cfg);
    }
    public void loadRules(){
        RedisProperties redisProperties = YamlUtil.configBind(PREFIX_REDIS,cfg.getDataSourceRef(),new RedisProperties());
        if (redisProperties == null) {
            return;
        }
        //复用原来的解析
        RedisConfiguration redisConfig = RedisAutoConfig.getRedisConfiguration(redisProperties);
        RedisConnectionConfig sentinelCfg = null;
        RedisConnectionConfig.Builder builder = RedisConnectionConfig.builder();
        if (redisConfig instanceof RedisClusterConfiguration) {
            RedisClusterConfiguration cluster =  (RedisClusterConfiguration) redisConfig;
            builder.withPassword(new String(cluster.getPassword().get()));
            for (RedisNode node : cluster.getClusterNodes()){
                builder.withRedisCluster(node.getHost(),node.getPort());
            }
            sentinelCfg = builder.build();
        }
        else if (redisConfig instanceof RedisSentinelConfiguration) {
            RedisSentinelConfiguration sentinel =  (RedisSentinelConfiguration) redisConfig;
            builder.withPassword(new String(sentinel.getPassword().get()))
                    .withDatabase(sentinel.getDatabase())
                    .withSentinelMasterId(sentinel.getMaster().getName());
            for (RedisNode node : sentinel.getSentinels()){
                builder.withRedisSentinel(node.getHost(),node.getPort());
            }
            sentinelCfg = builder.build();
        }else{
            RedisStandaloneConfiguration stand =  (RedisStandaloneConfiguration) redisConfig;
            sentinelCfg = builder.withHost(stand.getHostName())
                    .withPort(stand.getPort())
                    .withDatabase(stand.getDatabase())
                    .withPassword(new String(stand.getPassword().get())).build();
        }
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new RedisDataSource<>(sentinelCfg, cfg.getAppId() + DOT + FLOW_RULE_KEY,cfg.getNamespace(),
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new RedisDataSource<>(sentinelCfg, cfg.getAppId() + DOT + DEGRADE_RULES_KEY,cfg.getNamespace(),
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

        ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource = new RedisDataSource<>(sentinelCfg, cfg.getAppId() + DOT + AUTHORITY_RULES_KEY,cfg.getNamespace(),
                source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {}));
        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());

        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new RedisDataSource<>(sentinelCfg, cfg.getAppId() + DOT + PARAMFLOW_RULES_KEY,cfg.getNamespace(),
                source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());

        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new RedisDataSource<>(sentinelCfg, cfg.getAppId() + DOT + SYSTEM_RULES_KEY,cfg.getNamespace(),
                source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }
}
