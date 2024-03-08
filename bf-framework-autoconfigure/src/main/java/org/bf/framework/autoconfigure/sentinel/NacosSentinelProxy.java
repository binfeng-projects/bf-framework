package org.bf.framework.autoconfigure.sentinel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NacosSentinelProxy extends SentinelProxy{
    public NacosSentinelProxy(SentinelProperties cfg) {
        super(cfg);
    }
    public void loadRules(){
//        Properties properties = new Properties();
//        properties.put(PropertyKeyConst.SERVER_ADDR, cfg.getDataSourceRef());
//        properties.put(PropertyKeyConst.NAMESPACE, cfg.getNamespace());

//        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, cfg.getAppId() , FLOW_RULE_KEY,
//                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
//        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
//
//        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(properties, cfg.getAppId() , DEGRADE_RULES_KEY,
//                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
//        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
//
//        ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource = new NacosDataSource<>(properties, cfg.getAppId() , AUTHORITY_RULES_KEY,
//                source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {}));
//        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());
//
//        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new NacosDataSource<>(properties, cfg.getAppId() , PARAMFLOW_RULES_KEY,
//                source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
//        ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());
//
//        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new NacosDataSource<>(properties, cfg.getAppId() , SYSTEM_RULES_KEY,
//                source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
//        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }
}
