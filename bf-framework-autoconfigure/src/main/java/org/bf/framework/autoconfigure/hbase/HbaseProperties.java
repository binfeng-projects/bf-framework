package org.bf.framework.autoconfigure.hbase;

public class HbaseProperties {
	/**
	 * 依赖于哪个hadoop。引入哪个hadoop配置,可以为空
	 */
	private String hadoopRef;
	private String zookeeper;

	public String getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(String zookeeper) {
		this.zookeeper = zookeeper;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	private String tableName;
	private String familyName;
	public String getHadoopRef() {
		return hadoopRef;
	}
	public void setHadoopRef(String hadoopRef) {
		this.hadoopRef = hadoopRef;
	}
}
