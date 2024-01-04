package org.bf.framework.test.pojo;

import java.util.List;
public class TemplateTable {

	String sqlName;
	String javaName;
	String comment;

	List<TemplateColumn> columns;
	public TemplateTable() {}
	public TemplateTable(TemplateTable t) {
		setSqlName(t.getSqlName());
		this.comment = t.getComment();
		this.javaName = t.getJavaName();
		setColumns(t.getColumns());
	}
	
	public List<TemplateColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<TemplateColumn> columns) {
		this.columns = columns;
	}
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
	public void addColumn(TemplateColumn column) {
		columns.add(column);
	}
	public String getJavaName() {
		return javaName;
	}

	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}

}
