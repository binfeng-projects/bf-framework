package org.bf.framework.autoconfigure.yarn.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.yarn.am.grid.support.DefaultGridProjection;

import java.util.Map;

@ConfigurationProperties(value = "spring.yarn.appmaster")
public class SpringYarnAppmasterProperties {

	private boolean keepContextAlive = true;
	private int containerCount = 1;
	private String appmasterClass;
	private ContainerClusterProperties containercluster;
	private SpringYarnAppmasterResourceProperties resource;

	public SpringYarnAppmasterResourceProperties getResource() {
		return resource;
	}

	public void setResource(SpringYarnAppmasterResourceProperties resource) {
		this.resource = resource;
	}

	public SpringYarnAppmasterLaunchContextProperties getLaunchcontext() {
		return launchcontext;
	}

	public void setLaunchcontext(SpringYarnAppmasterLaunchContextProperties launchcontext) {
		this.launchcontext = launchcontext;
	}

	public SpringYarnAppmasterLocalizerProperties getLocalizer() {
		return localizer;
	}

	public void setLocalizer(SpringYarnAppmasterLocalizerProperties localizer) {
		this.localizer = localizer;
	}

	private SpringYarnAppmasterLaunchContextProperties launchcontext;
	private SpringYarnAppmasterLocalizerProperties localizer;
	public String getAppmasterClass() {
		return appmasterClass;
	}

	public void setAppmasterClass(String appmasterClass) {
		this.appmasterClass = appmasterClass;
	}

	public boolean isKeepContextAlive() {
		return keepContextAlive;
	}

	public void setKeepContextAlive(boolean keepContextAlive) {
		this.keepContextAlive = keepContextAlive;
	}

	public int getContainerCount() {
		return containerCount;
	}

	public void setContainerCount(int containerCount) {
		this.containerCount = containerCount;
	}

	public ContainerClusterProperties getContainercluster() {
		return containercluster;
	}

	public void setContainercluster(ContainerClusterProperties containercluster) {
		this.containercluster = containercluster;
	}

	public static class ContainerClusterProperties {

		private boolean enabled;

		private Map<String, ContainerClustersProperties> clusters;

		public Map<String, ContainerClustersProperties> getClusters() {
			return clusters;
		}

		public void setClusters(Map<String, ContainerClustersProperties> clusters) {
			this.clusters = clusters;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	public static class ContainerClustersProperties {

		private SpringYarnAppmasterResourceProperties resource;

		private SpringYarnAppmasterLaunchContextProperties launchcontext;

		private SpringYarnAppmasterLocalizerProperties localizer;

		private ContainerClustersProjectionProperties projection;

		public SpringYarnAppmasterResourceProperties getResource() {
			return resource;
		}

		public void setResource(SpringYarnAppmasterResourceProperties resource) {
			this.resource = resource;
		}

		public SpringYarnAppmasterLaunchContextProperties getLaunchcontext() {
			return launchcontext;
		}

		public void setLaunchcontext(SpringYarnAppmasterLaunchContextProperties launchcontext) {
			this.launchcontext = launchcontext;
		}

		public SpringYarnAppmasterLocalizerProperties getLocalizer() {
			return localizer;
		}

		public void setLocalizer(SpringYarnAppmasterLocalizerProperties localizer) {
			this.localizer = localizer;
		}

		public ContainerClustersProjectionProperties getProjection() {
			return projection;
		}

		public void setProjection(ContainerClustersProjectionProperties projection) {
			this.projection = projection;
		}

	}

	public static class ContainerClustersProjectionProperties {

		private String type = DefaultGridProjection.REGISTERED_NAME;

		private ContainerClustersProjectionDataProperties data;

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public void setData(ContainerClustersProjectionDataProperties data) {
			this.data = data;
		}

		public ContainerClustersProjectionDataProperties getData() {
			return data;
		}

	}

	public static class ContainerClustersProjectionDataProperties {

		private Integer any;

		private Map<String, Integer> hosts;

		private Map<String, Integer> racks;

		private Map<String, Object> properties;

		public Integer getAny() {
			return any;
		}

		public void setAny(Integer any) {
			this.any = any;
		}

		public Map<String, Integer> getHosts() {
			return hosts;
		}

		public void setHosts(Map<String, Integer> hosts) {
			this.hosts = hosts;
		}

		public Map<String, Integer> getRacks() {
			return racks;
		}

		public void setRacks(Map<String, Integer> racks) {
			this.racks = racks;
		}

		public Map<String, Object> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, Object> properties) {
			this.properties = properties;
		}

	}

}
