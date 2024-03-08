package org.bf.framework.autoconfigure.sentinel;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
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

import java.util.List;

import static org.bf.framework.boot.constant.FrameworkConst.*;

@Slf4j
public class ApolloSentinelProxy extends SentinelProxy{

    public ApolloSentinelProxy(SentinelProperties cfg) {
        super(cfg);
    }
    public void loadRules(){
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(cfg.getNamespace(), cfg.getAppId() + DOT + FLOW_RULE_KEY, DEFAULT_RULES,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new ApolloDataSource<>(cfg.getNamespace(), cfg.getAppId() + DOT + DEGRADE_RULES_KEY, DEFAULT_RULES,
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

        ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource = new ApolloDataSource<>(cfg.getNamespace(), cfg.getAppId() + DOT + AUTHORITY_RULES_KEY, DEFAULT_RULES,
                source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {}));
        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());

        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new ApolloDataSource<>(cfg.getNamespace(), cfg.getAppId() + DOT + PARAMFLOW_RULES_KEY, DEFAULT_RULES,
                source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());

        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new ApolloDataSource<>(cfg.getNamespace(), cfg.getAppId() + DOT + SYSTEM_RULES_KEY, DEFAULT_RULES,
                source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }
}
