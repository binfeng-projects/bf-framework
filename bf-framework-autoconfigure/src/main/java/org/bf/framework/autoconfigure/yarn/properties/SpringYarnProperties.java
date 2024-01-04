/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bf.framework.autoconfigure.yarn.properties;

import org.bf.framework.autoconfigure.hadoop.HadoopProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(value = "spring.yarn")
public class SpringYarnProperties {

	private String applicationDir;
	private String applicationBaseDir;
	private String applicationVersion;
	private String stagingDir;
	private String appName;
	private String appType;
	private String siteYarnAppClasspath;
	private String siteMapreduceAppClasspath;

	public HadoopProperties getHadoopProperties() {
		return hadoopProperties;
	}

	public void setHadoopProperties(HadoopProperties hadoopProperties) {
		this.hadoopProperties = hadoopProperties;
	}

	private HadoopProperties hadoopProperties;
	public String getBatchRef() {
		return batchRef;
	}

	public void setBatchRef(String batchRef) {
		this.batchRef = batchRef;
	}

	/**
	 * 引用哪个batch配置
	 */
	private String batchRef;
	public String getHadoopRef() {
		return hadoopRef;
	}

	public void setHadoopRef(String hadoopRef) {
		this.hadoopRef = hadoopRef;
	}

	public SpringYarnHostInfoDiscoveryProperties getHostdiscovery() {
		return hostdiscovery;
	}

	public void setHostdiscovery(SpringYarnHostInfoDiscoveryProperties hostdiscovery) {
		this.hostdiscovery = hostdiscovery;
	}

	public SpringYarnAppmasterProperties getAppmaster() {
		return appmaster;
	}

	public void setAppmaster(SpringYarnAppmasterProperties appmaster) {
		this.appmaster = appmaster;
	}

	public SpringYarnClientProperties getClient() {
		return client;
	}

	public void setClient(SpringYarnClientProperties client) {
		this.client = client;
	}

	public SpringYarnContainerProperties getContainer() {
		return container;
	}

	public void setContainer(SpringYarnContainerProperties container) {
		this.container = container;
	}

	public SpringYarnEnvProperties getSyep() {
		return syep;
	}

	public void setSyep(SpringYarnEnvProperties syep) {
		this.syep = syep;
	}

	/**
	 * yarn依赖于hadoop。引入哪个hadoop配置
	 */
	private String hadoopRef;

	private SpringYarnHostInfoDiscoveryProperties hostdiscovery;
	private SpringYarnAppmasterProperties appmaster;
	private SpringYarnClientProperties client;
	private SpringYarnContainerProperties container;
	private SpringYarnEnvProperties syep;
	public String getApplicationDir() {
		return applicationDir;
	}

	public void setApplicationDir(String applicationDir) {
		this.applicationDir = applicationDir;
	}

	public String getApplicationBaseDir() {
		return applicationBaseDir;
	}

	public void setApplicationBaseDir(String applicationBaseDir) {
		this.applicationBaseDir = applicationBaseDir;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public String getStagingDir() {
		return stagingDir;
	}

	public void setStagingDir(String stagingDir) {
		this.stagingDir = stagingDir;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getSiteYarnAppClasspath() {
		return siteYarnAppClasspath;
	}

	public void setSiteYarnAppClasspath(String siteYarnAppClasspath) {
		this.siteYarnAppClasspath = siteYarnAppClasspath;
	}

	public String getSiteMapreduceAppClasspath() {
		return siteMapreduceAppClasspath;
	}

	public void setSiteMapreduceAppClasspath(String siteMapreduceAppClasspath) {
		this.siteMapreduceAppClasspath = siteMapreduceAppClasspath;
	}

}
