[#include "common.ftl"]
package ${currentPackage};
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.bf.framework.autoconfigure.elasticsearch.BaseElasticsearchEntity;
import ${corePackage}.Constant;
[#list importClass as cls]
import ${cls};
[/#list]
/**
 * ${table.comment}
 */
@Data
@Document(indexName = "${table.sqlName}")
public class ${className}${classSuffix} extends BaseElasticsearchEntity<${pkType}>{

[#list table.columns as field]
    /**
     * ${(field.comment)!}
     */
  [#if field.javaType=='String']
//    @Field(name = "${field.sqlName}",type = FieldType.Keyword, index = false)
    @Field(name = "${field.sqlName}",type = FieldType.Text, analyzer = Constant.DEFAULT_SEARCH_ANALYZER)
  [#elseif field.javaType=='LocalDateTime']
    @Field(name = "${field.sqlName}",type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
  [#elseif field.javaType=='Object']
  [#else]
    @Field(name = "${field.sqlName}",type = FieldType.${field.javaType})
  [/#if]
    private ${field.javaType} ${field.javaName};
[/#list]

}
