
[#assign className=table.className  /]
[#assign classNameLower = table.classNameFirstLower  /]

[#--
#t 指示FreeMarker去忽略标记中行的特定的空白
--]
${gg.setOverride(false)}[#t  /]
${gg.setOutputFile(serverPath + "src/test/java/" + serverPackage_dir + "/test/Test" + className + "Service.java")}[#t  /]
package ${serverPackage}.test;

import ${dtoPackage}.${className}${dtoName};
import ${servicePackage}.convert.${className}Convert;
import ${servicePackage}.${className}${serviceName};
import ${serverPackage}.BaseServiceTest;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;
import org.mockito.*;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Test${className}Service extends BaseServiceTest<${className}${serviceName},${className}Convert> {

    @Autowired
    @InjectMocks
    ${className}${serviceName} service;

[#if pkJavaType=='Long']
    static ${pkJavaType} pkId = 1l;
[#elseif pkJavaType=='Integer']
    static ${pkJavaType} pkId = 1;
[#else]
    static ${pkJavaType} pkId = "test111";
[/#if]

    public ${className}${serviceName} createSvc(){
        //不在spring环境也不在mock环境中时候，需要自己new
        return service = service == null ? super.createSvc() : service;
    }

    @Test
    public void testList(){
        print("${ctlUrlPrefix}/list");
        printJSON(service.listPage(null));
    }

    @Test
    public void testAdd(){
        ${className}${dtoName} p = new ${className}${dtoName}();
    [#list table.columns as field]
        [#if field.asType=='String']
        p.set${field.columnNameFirstUpper}("${(field.remarks)!}");
        [#elseif field.asType=='Long']
        p.set${field.columnNameFirstUpper}(1L);
        [#elseif field.asType=='Integer']
        p.set${field.columnNameFirstUpper}(1);
        [#elseif field.asType=='Boolean']
        p.set${field.columnNameFirstUpper}(false);
        [#elseif field.asType=='Enum']
        p.set${field.columnNameFirstUpper}(${field.simpleJavaType}.);
        [#elseif field.asType=='Date']
        p.set${field.columnNameFirstUpper}(new Date());
        [#elseif field.asType=='BigDecimal']
        p.set${field.columnNameFirstUpper}(new BigDecimal("1.22"));
        [#elseif field.asType=='Object']
        ${field.simpleJavaType} obj = new ${field.simpleJavaType}();
        p.set${field.columnNameFirstUpper}(obj);
        [#else]
        //TODO fixme
        p.set${field.columnNameFirstUpper}("${(field.remarks)!}");
        [/#if]
    [/#list]
        print("-------------------web接口地址------------------------");
        print("${ctlUrlPrefix}/save");

        printJSON(p);
        printJSON(service.save(p));
    }

    @Test
    public void testEdit()
    {
        ${className}${dtoName} p = new ${className}${dtoName}();
        p.setId(pkId);
    [#list table.columns as field]
        [#if field.asType=='String']
        p.set${field.columnNameFirstUpper}("${(field.remarks)!}");
        [#elseif field.asType=='Long']
        p.set${field.columnNameFirstUpper}(1L);
        [#elseif field.asType=='Integer']
        p.set${field.columnNameFirstUpper}(1);
        [#elseif field.asType=='Boolean']
        p.set${field.columnNameFirstUpper}(false);
        [#elseif field.asType=='Enum']
        p.set${field.columnNameFirstUpper}(${field.simpleJavaType}.);
        [#elseif field.asType=='Date']
        p.set${field.columnNameFirstUpper}(new Date());
        [#elseif field.asType=='BigDecimal']
        p.set${field.columnNameFirstUpper}(new BigDecimal("1.22"));
        [#elseif field.asType=='Object']
        ${field.simpleJavaType} obj = new ${field.simpleJavaType}();
        p.set${field.columnNameFirstUpper}(obj);
        [#else]
        //TODO fixme
        p.set${field.columnNameFirstUpper}("${(field.remarks)!}");
        [/#if]
    [/#list]
        print("-------------------web接口地址------------------------");
        print("${ctlUrlPrefix}/edit");

        printJSON(p);
        printJSON(service.edit(p));
    }

    @Test
    public void testRemoveById() {
        printJSON(service.removeById(pkId));
    }

    @Test
    public void testGet()
    {
        ${className}${dtoName} p = new ${className}${dtoName}();
        p.setId(pkId);
        print("-------------------web接口地址------------------------");
        print("${ctlUrlPrefix}/get");

        printJSON(p);
        printJSON(service.get(p));
    }
}
