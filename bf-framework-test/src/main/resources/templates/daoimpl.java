[#include "common.ftl"]

@Override
public Condition dynamicCondition(${entity} e) {
    Condition cd = DSL.trueCondition();
    [#list table.columns as field]
    [#if field.javaType=='String']
    if(StringUtils.isNotBlank(e.get${field.javaName?cap_first}())) {
        cd.and(${table.sqlName?upper_case}.${field.sqlName?upper_case}.eq(e.get${field.javaName?cap_first}()));
    }
    [#elseif field.javaType=='Long']
    [#elseif field.javaType=='Integer']
    [#elseif field.javaType=='Boolean']
    [#elseif field.javaType=='Enum']
    [#elseif field.javaType=='Date']
    [#elseif field.javaType=='BigDecimal']
    [#elseif field.javaType=='Object']
    [#else]
    if(null != e.get${field.javaName?cap_first}()) {
        cd.and(${table.sqlName?upper_case}.${field.sqlName?upper_case}.eq(e.get${field.javaName?cap_first}()));
    }
    [/#if]
    [/#list]
    return cd;
}