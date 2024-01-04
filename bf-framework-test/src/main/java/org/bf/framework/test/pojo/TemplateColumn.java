package org.bf.framework.test.pojo;

import lombok.Data;

@Data
public class TemplateColumn {
    private static final String BOOLEAN_PREFIX = "is_";

	public TemplateColumn() {
	}
	private String javaName;

	private String sqlName;

	private String comment;
	private String javaType;
	private String sqlType;
	private String enumClassName;
	/**
	 * public enum ${enumClassName} {
	 * 		${enumAlias}(${enumKey},${enumDesc});
	 * 		private String key;
	 * 		private String value;
	 * }
	 * @author badqiu
	 */
	public static class EnumMetaDada {
		private String enumAlias;
		private String enumKey;
		private String enumDesc;
		public EnumMetaDada(String enumAlias, String enumKey, String enumDesc) {
			super();
			this.enumAlias = enumAlias;
			this.enumKey = enumKey;
			this.enumDesc = enumDesc;
		}
		
		public String getEnumAlias() {
			return enumAlias;
		}
		public String getEnumKey() {
			return enumKey;
		}
		public String getEnumDesc() {
			return enumDesc;
		}
	}
}
