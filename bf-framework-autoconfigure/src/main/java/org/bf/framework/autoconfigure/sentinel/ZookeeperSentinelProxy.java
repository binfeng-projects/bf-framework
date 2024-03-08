package org.bf.framework.autoconfigure.sentinel;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
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
import org.bf.framework.autoconfigure.zookeeper.ZookeeperProperties;
import org.bf.framework.boot.util.YamlUtil;

import java.util.List;

import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_ZOOKEEPER;

@Slf4j
public class ZookeeperSentinelProxy extends SentinelProxy{

    public ZookeeperSentinelProxy(SentinelProperties cfg) {
        super(cfg);
    }
    public void loadRules(){
        ZookeeperProperties properties = YamlUtil.configBind(PREFIX_ZOOKEEPER,cfg.getDataSourceRef(),new ZookeeperProperties());
        if (properties == null) {
            return;
        }
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<>(properties.getUrl(), cfg.getAppId() , FLOW_RULE_KEY,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new ZookeeperDataSource<>(properties.getUrl(), cfg.getAppId() , DEGRADE_RULES_KEY,
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

        ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource = new ZookeeperDataSource<>(properties.getUrl(), cfg.getAppId() , AUTHORITY_RULES_KEY,
                source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {}));
        AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());

        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new ZookeeperDataSource<>(properties.getUrl(), cfg.getAppId() , PARAMFLOW_RULES_KEY,
                source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());

        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new ZookeeperDataSource<>(properties.getUrl(), cfg.getAppId() , SYSTEM_RULES_KEY,
                source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
    }
}
