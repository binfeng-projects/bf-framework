[#include "common.ftl"]

public class Test${daoProxy} extends BaseProxyTest<${pkType},${clientDto},${entity}> {

[#if pkType=='Long']
    static ${pkType} pkId = 1l;
[#elseif pkType=='Integer']
    static ${pkType} pkId = 1;
[#else]
    static ${pkType} pkId = "test111";
[/#if]

    @Test
    public void testList(){
        info(daoProxy.listPage(null));
    }

    @Test
    public void testSave(){
        ${clientDto} p = genePojo();
        info(p);
        daoProxy.save(p);
    }

    @Test
    public void testEdit(){
        ${clientDto} p = genePojo();
        p.setId(pkId);
        info(p);
        daoProxy.edit(p);
    }
    public static ${clientDto} genePojo()
    {
        ${clientDto} p = new ${clientDto}();
    [#list table.columns as field]
        [#if field.javaType=='String']
        p.set${field.javaName?cap_first}("${(field.comment)!}");
        [#elseif field.javaType=='Long']
        p.set${field.javaName?cap_first}(1L);
        [#elseif field.javaType=='Integer']
        p.set${field.javaName?cap_first}(1);
        [#elseif field.javaType=='Boolean']
        p.set${field.javaName?cap_first}(false);
        [#elseif field.javaType=='Enum']
        p.set${field.javaName?cap_first}(${field.javaType}.);
        [#elseif field.javaType=='Date']
        p.set${field.javaName?cap_first}(new Date());
        [#elseif field.javaType=='BigDecimal']
        p.set${field.javaName?cap_first}(new BigDecimal("1.22"));
        [#elseif field.javaType=='Object']
        [#else]
        //TODO fixme
        p.set${field.javaName?cap_first}("${(field.comment)!}");
        [/#if]
    [/#list]
        return p;
    }
    @Test
    public void testRemoveById() {
        daoProxy.removeById(pkId);
    }

    @Test
    public void testGet(){
        ${clientDto} p = new ${clientDto}();
        p.setId(pkId);
        info(p);
        info(daoProxy.get(p));
    }

}
