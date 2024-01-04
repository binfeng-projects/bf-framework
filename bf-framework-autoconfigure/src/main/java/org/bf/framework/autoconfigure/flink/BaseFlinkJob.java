package org.bf.framework.autoconfigure.flink;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.client.deployment.application.WebSubmissionJobClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.bf.framework.boot.config.SystemHelp;
import org.bf.framework.boot.spi.EnvironmentPropertyPostProcessor;
import org.bf.framework.boot.util.CommandLineUtil;
import org.bf.framework.common.util.MapUtils;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BaseFlinkJob implements EnvironmentPropertyPostProcessor {

    private String jobName;
    public BaseFlinkJob(String jobName) {
        if(StringUtils.isBlank(jobName)) {
            throw new RuntimeException("jobName empty");
        }
        this.jobName = jobName;
    }
    protected StreamExecutionEnvironment env;

    protected StreamTableEnvironment tableEnv;

    public abstract void processJob();
    public void run(String[] args) {
        configEnv(args);
        processJob();
        String finalJobName = StringUtils.join(jobName, "-", new DateTime().toString("yyyyMMdd-HHmmss"));
        try {
//            env.execute(finalJobName);
            //提交任务
            JobClient jobClient = env.executeAsync(finalJobName);
            if (jobClient instanceof WebSubmissionJobClient) {
                return;
            }
            jobClient.getJobExecutionResult()
                    .whenComplete((jobExecutionResult, throwable) -> log.info("time {}", jobExecutionResult.getNetRuntime(TimeUnit.SECONDS)));
        } catch (Exception e) {
            log.error("job {} exe error",jobName,e);
        }
    }
    private void configEnv(String[] args) {
        Map<String,String> cfgMap = new HashMap<>();
        //配置文件
        Map<String,Object> property = SystemHelp.loadIfConfigFileProperty(classPathConfigFileKey());
        if(MapUtils.isEmpty(property)) {
            property = simpleProperties();
        }
        if(MapUtils.isNotEmpty(property)) {
            property.forEach((s,o)-> cfgMap.put(s,String.valueOf(o)));
        }
        //命令行优先级更高
        //解析flink参数
        ParameterTool flinkArgs = ParameterTool.fromArgs(args);
        //合并两种参数
        Map<String, String> flinkArgsMap = flinkArgs.toMap();
        for (Map.Entry<String, String> entry : flinkArgsMap.entrySet()) {
            if (Objects.equals(entry.getValue(), "__NO_VALUE_KEY")) {
                continue;
            }
            cfgMap.put(entry.getKey(), entry.getValue());
        }
        //解析spring参数
        cfgMap.putAll(CommandLineUtil.resolveArgs(args));
        Configuration config = Configuration.fromMap(cfgMap);
        env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        env.getConfig().setGlobalJobParameters(config);
        tableEnv = StreamTableEnvironment.create(env);

        String parallelism = flinkArgs.get("parallelism");
        if (StringUtils.isNotBlank(parallelism)) {
            env.setParallelism(Integer.valueOf(parallelism));
        }
    }
}
