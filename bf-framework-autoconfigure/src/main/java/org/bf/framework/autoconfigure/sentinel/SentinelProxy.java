package org.bf.framework.autoconfigure.sentinel;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.util.SpringUtil;

@Slf4j
public abstract class SentinelProxy {
    public static final String DEFAULT_RULES = "[]";
    public static final String FLOW_RULE_KEY = "FlowRules";
    public static final String DEGRADE_RULES_KEY = "DegradeRules";
    public static final String AUTHORITY_RULES_KEY = "AuthorityRules";
    public static final String PARAMFLOW_RULES_KEY = "ParamFlowRules";
    public static final String SYSTEM_RULES_KEY = "SystemRules";

    protected SentinelProperties cfg;
    public SentinelProxy(SentinelProperties cfg) {
        this.cfg = cfg;
        cfg.setAppId(SpringUtil.appName());
    }
    public abstract void loadRules();
}
