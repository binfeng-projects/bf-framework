package org.bf.framework.autoconfigure.yarn;

import org.apache.hadoop.conf.Configuration;
import org.bf.framework.autoconfigure.batch.BatchProxy;
import org.springframework.yarn.am.YarnAppmaster;
import org.springframework.yarn.batch.support.YarnJobLauncher;
import org.springframework.yarn.boot.support.AppmasterLauncherRunner;
import org.springframework.yarn.boot.support.ContainerLauncherRunner;
import org.springframework.yarn.client.YarnClient;
import org.springframework.yarn.container.YarnContainer;
import org.springframework.yarn.fs.ResourceLocalizer;

import java.util.Map;

public class YarnProxy {
    private Configuration yarnConfiguration;
    private Map<String, String> yarnEnvironment;
    private ResourceLocalizer resourceLocalizer;
    private AppmasterLauncherRunner appmasterLauncherRunner;
    private ContainerLauncherRunner containerLauncherRunner;
    private YarnClient yarnClient;
    private BatchProxy batchProxy;

    public YarnAppmaster getYarnAppmaster() {
        return yarnAppmaster;
    }

    public void setYarnAppmaster(YarnAppmaster yarnAppmaster) {
        this.yarnAppmaster = yarnAppmaster;
    }

    public YarnContainer getYarnContainer() {
        return yarnContainer;
    }

    public void setYarnContainer(YarnContainer yarnContainer) {
        this.yarnContainer = yarnContainer;
    }

    private YarnJobLauncher yarnJobLauncher;

    private YarnAppmaster yarnAppmaster;
    private YarnContainer yarnContainer;
    public AppmasterLauncherRunner getAppmasterLauncherRunner() {
        return appmasterLauncherRunner;
    }

    public void setAppmasterLauncherRunner(AppmasterLauncherRunner appmasterLauncherRunner) {
        this.appmasterLauncherRunner = appmasterLauncherRunner;
    }

    public ContainerLauncherRunner getContainerLauncherRunner() {
        return containerLauncherRunner;
    }

    public void setContainerLauncherRunner(ContainerLauncherRunner containerLauncherRunner) {
        this.containerLauncherRunner = containerLauncherRunner;
    }

    public BatchProxy getBatchProxy() {
        return batchProxy;
    }

    public void setBatchProxy(BatchProxy batchProxy) {
        this.batchProxy = batchProxy;
    }

    public Configuration getYarnConfiguration() {
        return yarnConfiguration;
    }

    public void setYarnConfiguration(Configuration yarnConfiguration) {
        this.yarnConfiguration = yarnConfiguration;
    }

    public Map<String, String> getYarnEnvironment() {
        return yarnEnvironment;
    }

    public void setYarnEnvironment(Map<String, String> yarnEnvironment) {
        this.yarnEnvironment = yarnEnvironment;
    }

    public ResourceLocalizer getResourceLocalizer() {
        return resourceLocalizer;
    }

    public void setResourceLocalizer(ResourceLocalizer resourceLocalizer) {
        this.resourceLocalizer = resourceLocalizer;
    }
    public YarnClient getYarnClient() {
        return yarnClient;
    }

    public void setYarnClient(YarnClient yarnClient) {
        this.yarnClient = yarnClient;
    }


    public YarnJobLauncher getYarnJobLauncher() {
        return yarnJobLauncher;
    }

    public void setYarnJobLauncher(YarnJobLauncher yarnJobLauncher) {
        this.yarnJobLauncher = yarnJobLauncher;
    }

}
