package org.bf.framework.autoconfigure.hive;

public class HiveProperties {
	/**
	 * 依赖于哪个hadoop。引入哪个hadoop配置,可以为空
	 */
	private String hadoopRef;

	public String getHiveUrl() {
		return hiveUrl;
	}

	public void setHiveUrl(String hiveUrl) {
		this.hiveUrl = hiveUrl;
	}

	private String hiveUrl;
	public String getHadoopRef() {
		return hadoopRef;
	}

	public void setHadoopRef(String hadoopRef) {
		this.hadoopRef = hadoopRef;
	}
}
