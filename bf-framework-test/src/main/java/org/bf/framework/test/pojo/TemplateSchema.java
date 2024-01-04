package org.bf.framework.test.pojo;

public class TemplateSchema {

	String sqlName;
	String javaName;
	String comment;
	public TemplateSchema() {}
	public String getSqlName() {
		return sqlName;
	}
	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getJavaName() {
		return javaName;
	}
	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}

}
