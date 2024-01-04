package org.bf.framework.test.jooq;

import freemarker.template.Configuration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.jooq.BaseDaoImpl;
import org.bf.framework.boot.base.BaseConvert;
import org.bf.framework.boot.base.BaseDaoProxy;
import org.bf.framework.boot.base.PermCheck;
import org.bf.framework.boot.util.FreemarkerUtil;
import org.bf.framework.common.base.BaseDTO;
import org.bf.framework.common.base.BaseDao;
import org.bf.framework.common.base.BaseEntity;
import org.bf.framework.common.result.PageResult;
import org.bf.framework.common.result.Result;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.bf.framework.test.base.BaseProxyTest;
import org.bf.framework.test.codegen.CodeGenTool;
import org.bf.framework.test.pojo.TemplateColumn;
import org.bf.framework.test.pojo.TemplateTable;
import org.jooq.Condition;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.impl.DSL;
import org.jooq.meta.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.bf.framework.test.codegen.CodeGenTool.MIDDLEWARE_DIR;
import static org.bf.framework.test.codegen.CodeGenTool.TARGET_DIR;
import static org.jooq.codegen.GeneratorStrategy.Mode;

@Slf4j
public class DatasourceJavaGenerator extends JavaGenerator {
    protected static FreemarkerUtil freemarkerUtil;
    protected static Map<String,Map<String,Object>> tableModelMap = MapUtils.newConcurrentHashMap();
    static {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
            cfg.setSettings(new ClassPathResource("freemarker.properties").getInputStream());
            cfg.setEncoding(Locale.SIMPLIFIED_CHINESE, "UTF-8");
            cfg.setClassForTemplateLoading(DatasourceJavaGenerator.class, "/templates");
            cfg.setDefaultEncoding("UTF-8");
            freemarkerUtil = new FreemarkerUtil(cfg);
        } catch (Exception e) {

        }
    }
//    @Override
//    protected void generateDao(TableDefinition table) {
//        super.generateDao(table);
//        File file = getFile(table, GeneratorStrategy.Mode.DAO);
//        if (file.exists()) {
//            try {
//                String fileContent = new String(FileCopyUtils.copyToByteArray(file));
//                String oldExtends = " extends DAOImpl";
//                String newExtends = " extends UserCenterBaseDaoImpl";
//                fileContent = fileContent.replace("import org.jooq.impl.DAOImpl;\n", "");
//                fileContent = fileContent.replace(oldExtends, newExtends);
//                FileCopyUtils.copy(fileContent.getBytes(), file);
//            } catch (IOException e) {
//                log.error("generateDao error: {}", file.getAbsolutePath(), e);
//            }
//        }
//    }
//
//    @Override
//    protected void generateDao(TableDefinition table, JavaWriter out) {
//        // 用于生成 import com.diamondfsd.jooq.learn.extend.AbstractExtendDAOImpl 内容
//        out.ref(UserCenterBaseDaoImpl.class);
//        super.generateDao(table, out);
//
//    }
    @Override
    protected void generateDao(TableDefinition table) {
        JavaWriter out = newJavaWriter(getFile(table, Mode.DAO));
        final String daoName = refClassName(table, Mode.DAO,out);
        final String tableRecord = refClassName(table, Mode.RECORD,out);
        final String fullTableName = getStrategy().getFullJavaClassName(table);
        final String tableIdentifier = table.getName().toUpperCase();

        String baseDaoInterface = refClassName(table, Mode.CORE_INTERFACE,out);
        String entityName = refClassName(table,Mode.POJO,out);
        String tType = out.ref(getPrimaryKeyType(table,out));
        String baseDaoJavaName = out.ref(getStrategy().getFullJavaClassName(table.getSchema(), Mode.CORE_JOOQBASEDAO));;
        printPackage(out, table, Mode.DAO);
        out.println("import static %s.%s;", fullTableName,tableIdentifier);
        generateDaoClassJavadoc(table, out);
        out.println("@%s", out.ref("org.springframework.stereotype.Repository"));
        out.println("%sclass %s extends %s<%s, %s, %s> implements %s {",
                PUBLIC, daoName, baseDaoJavaName, tableRecord, entityName, tType,baseDaoInterface);
        // Default constructor
        // -------------------
        out.println("%s%s() {", PUBLIC, daoName);
        out.println("super(%s, %s.class);", tableIdentifier, entityName);
        out.println("}");

        //------------------dynamicCondition方法-------------------
        out.ref(org.bf.framework.common.util.StringUtils.class);
        out.ref(Condition.class);
        out.ref(DSL.class);
        out.println(freemarkerUtil.renderTemplate("daoimpl.java",generateTemplateModel(table)));
        out.println("}");
        closeJavaWriter(out);
        generateDaoClassFooter(table, out);
    }
    @Override
    protected void generatePojo(TableDefinition table, JavaWriter out) {
        Mode mode = Mode.POJO;
        out.ref(BaseEntity.class);
        final String className = getStrategy().getJavaClassName(table, mode);
        final String interfaceName = generateInterfaces()
                ? out.ref(getStrategy().getFullJavaClassName(table, Mode.INTERFACE))
                : "";
        printPackage(out, table, mode);
        generatePojoClassJavadoc(table, out);
        String tType = out.ref(getPrimaryKeyType(table,out));
        out.println("%sclass %s extends %s<%s> implements %s {",
                PUBLIC, className, BaseEntity.class.getSimpleName(), tType,interfaceName);
        out.println();
        for (TypedElementDefinition<?> column : filterTypedElements(table,true)) {
            out.javadoc(column.getComment());
            out.println("private %s %s;",
                    out.ref(getJavaType(column.getType(resolver(out, mode)), out, mode)),
                    getStrategy().getJavaMemberName(column, mode));
        }
        // Constructors
        // ---------------------------------------------------------------------
        generatePojoDefaultConstructor(table, out);
        if (!generatePojosAsJavaRecordClasses()) {

            // [#1363] [#7055] copy constructor
            generatePojoCopyConstructor(table, out);

            // Multi-constructor
            generatePojoMultiConstructor(table, out);
        }

        List<? extends TypedElementDefinition<?>> elements = filterTypedElements(table,true);
        for (int i = 0; i < elements.size(); i++) {
            TypedElementDefinition<?> column = elements.get(i);
            out.println();
            generatePojoGetter(column, i, out);
            out.println();
            generatePojoSetter(column, i, out);
        }
//        generatePojoEqualsAndHashCode(table, out);
//        generatePojoToString(table, out);
        printFromAndInto(out, table);
        generatePojoClassFooter(table, out);

        out.println("public String getSchemaName() {");
        out.println("return \"%s\";", table.getSchema().getName());
        out.println("}");

        out.println("public String getTableName() {");
        out.println("return \"%s\";", table.getName());
        out.println("}");


        out.println("}");
        closeJavaWriter(out);

    }
    protected String getPrimaryKeyType(TableDefinition table, JavaWriter out){
        UniqueKeyDefinition key = table.getPrimaryKey();
        if (key == null) {
            return "String";
        }
        List<ColumnDefinition> keyColumns = key.getKeyColumns();
        String result = getJavaType(keyColumns.get(0).getType(resolver(out)), out, Mode.POJO);
        if (StringUtils.isBlank(result)) {
            result = "String";
        }
        return result;
    }
    public static List<ColumnDefinition> filterTypedElements(TableDefinition definition,boolean filter) {
        List<ColumnDefinition> colDefins = definition.getColumns();
        if(filter) {
            return colDefins.stream().filter(def-> !ignoreFileds.contains(def.getName())).toList();
        }
        return colDefins;
    }

    public static Set<String> ignoreFileds = CollectionUtils.newHashSet("id","version","created_at","updated_at");

    public static final String PUBLIC = "public ";

    @Override
    public void generatePojoGetter(TypedElementDefinition<?> column, @SuppressWarnings("unused") int index, JavaWriter out) {
        Mode mode = Mode.POJO;
        final String columnTypeFull = getJavaType(column.getType(resolver(out, mode)), out, mode);
        final String columnType = out.ref(columnTypeFull);
        final String columnGetter = getStrategy().getJavaGetterName(column, mode);
        final String columnMember = getStrategy().getJavaMemberName(column, mode);
        out.println("%s%s %s() {", PUBLIC, columnType, columnGetter);
        out.println("return this.%s;", columnMember);
        out.println("}");
    }
    protected void generatePojoSetter(TypedElementDefinition<?> column, @SuppressWarnings("unused") int index, JavaWriter out) {
        Mode mode = Mode.POJO;
        final String columnTypeFull = getJavaType(column.getType(resolver(out, mode)), out, mode);
        final String columnType = out.ref(columnTypeFull);
        final String columnSetter = getStrategy().getJavaSetterName(column, mode);
        final String columnMember = getStrategy().getJavaMemberName(column, mode);
        out.println("%s%s %s(%s %s) {", PUBLIC, "void", columnSetter,columnType, columnMember);
        out.println("this.%s = %s;", columnMember, columnMember);

        if (generateFluentSetters())
            out.println("return this;");

        out.println("}");
    }
    //--------------------------分割线，以上是覆盖系统的，以下是额外自定义的----------------------------------

    /**
     * 表级别的扩展
     * 因为父类的ClassFooter都是空实现，所以在generateXXXClassFooter中扩展自定义的，不会造成什么污染
     * @param table 我们其实只要table信息
     * @param ignore JavaWriter肯定是新new一个，不用传进来
     */
    @Override
    protected void generateDaoClassFooter(TableDefinition table, JavaWriter ignore) {
        generateCoreInterface(table);
        generateXmlGene(table);
        generateXmlManual(table);
        generateCoreConvert(table);
        generateCoreDaoProxy(table);
        generateCoreTest(table);
        generateServerCtl(table);
        generateServerRpc(table);
        generateClientRpc(table);
        generateClientDto(table);
    }
    protected void generateServerRpc(TableDefinition table) {
        Mode mode = Mode.SERVER_RPC;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(Slf4j.class);
        out.ref(Autowired.class);
        out.ref("org.apache.dubbo.config.annotation.DubboService");
        out.ref(PageResult.class);
        out.ref(Result.class);
        refClassName(table,Mode.CORE_DAOPROXY,out);
        refClassName(table,Mode.CLIENT_DTO,out);
        refClassName(table,Mode.CLIENT_RPC,out);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("rpc_impl.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void generateClientRpc(TableDefinition table) {
        Mode mode = Mode.CLIENT_RPC;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(PageResult.class);
        out.ref(Result.class);
        refClassName(table,Mode.CLIENT_DTO,out);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("rpc_api.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void generateServerCtl(TableDefinition table) {
        Mode mode = Mode.SERVER_CTL;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(Slf4j.class);
        out.ref(RestController.class);
        out.ref(RequestMapping.class);
        out.ref(PostMapping.class);
        out.ref(PermCheck.class);
        out.ref(RequestBody.class);
        out.ref(Autowired.class);
        out.ref(List.class);
        out.ref(PageResult.class);
        out.ref(Result.class);
        refClassName(table,Mode.CLIENT_DTO,out);
        refClassName(table,Mode.CORE_DAOPROXY,out);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("ctl.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void generateCoreDaoProxy(TableDefinition table) {
        Mode mode = Mode.CORE_DAOPROXY;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseDaoProxy.class);
        String tType = out.ref(getPrimaryKeyType(table,out));
        String dtoType = refClassName(table,Mode.CLIENT_DTO,out);
        String entityType = refClassName(table,Mode.POJO,out);
        printPackage(out, table, mode);
        final String className = getStrategy().getJavaClassName(table, mode);
        out.println("@%s", out.ref(Component.class));
        out.println("%sclass %s extends %s<%s,%s,%s> {",
                PUBLIC, className, BaseDaoProxy.class.getSimpleName(), tType,dtoType, entityType);
        out.println("}");
        closeJavaWriter(out);
    }
    protected void generateCoreTest(TableDefinition table) {
        Mode mode = Mode.CORE_TEST;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseProxyTest.class);
        refClassName(table,Mode.CLIENT_DTO,out);
        refClassName(table, Mode.POJO,out);
        out.ref(Test.class);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("coretest.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void generateCoreInterface(TableDefinition table) {
        Mode mode = Mode.CORE_INTERFACE;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseDao.class);
        String entityType = refClassName(table, Mode.POJO,out);
        String tType = out.ref(getPrimaryKeyType(table,out));
        printPackage(out, table, mode);
        generateDaoClassJavadoc(table, out);
//        out.println("@%s", out.ref("org.springframework.stereotype.Repository"));
        final String className = getStrategy().getJavaClassName(table, mode);
        out.println("%sinterface %s extends %s<%s, %s> {",
                PUBLIC, className, BaseDao.class.getSimpleName(), tType, entityType);

        out.println("}");
        closeJavaWriter(out);
    }
    protected void generateCoreConvert(TableDefinition table) {
        Mode mode = Mode.CORE_CONVERT;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseConvert.class);
        String tType = out.ref(getPrimaryKeyType(table,out));
        String dtoType = refClassName(table,Mode.CLIENT_DTO,out);
        String entityType = refClassName(table, Mode.POJO,out);
        printPackage(out, table, mode);
        final String className = getStrategy().getJavaClassName(table, mode);
        out.println("@%s(componentModel = \"spring\")", out.ref(Mapper.class));
        out.println("%sinterface %s extends %s<%s,%s,%s> {",
                PUBLIC, className, BaseConvert.class.getSimpleName(), tType,dtoType, entityType);
        out.println("}");
        closeJavaWriter(out);
    }
    protected void generateXmlManual(TableDefinition table) {
        Mode mode = Mode.CORE_XML_MANUAL;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        String daoFullName = getStrategy().getFullJavaClassName(table, Mode.CORE_INTERFACE);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        out.println("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >");
        out.println("<mapper namespace=\"%s\" >", daoFullName);
        out.println();
        out.println();
        out.println();
        out.println("</mapper>");
        closeJavaWriter(out);
    }
    protected void generateXmlGene(TableDefinition table) {
        Mode mode = Mode.CORE_XML_GENE;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.println(freemarkerUtil.renderTemplate("generate.xml",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void generateClientDto(TableDefinition table) {
        Mode mode = Mode.CLIENT_DTO;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseDTO.class);
        final String className = getStrategy().getJavaClassName(table, mode);
        printPackage(out, table, mode);
        generatePojoClassJavadoc(table, out);
        String tType = out.ref(getPrimaryKeyType(table,out));
        out.println("@%s", out.ref(Data.class));
        out.println("%sclass %s extends %s<%s> {",
                PUBLIC, className, BaseDTO.class.getSimpleName(), tType);
        out.println();
        for (TypedElementDefinition<?> column : filterTypedElements(table,true)) {
            out.javadoc(column.getComment());
            out.println("private %s %s;",
                    out.ref(getJavaType(column.getType(resolver(out, mode)), out, mode)),
                    getStrategy().getJavaMemberName(column, mode));
        }
        out.println("}");
        closeJavaWriter(out);
    }

    /**
     * schema级别的扩展
     */
    @Override
    protected void generateSchemaClassFooter(SchemaDefinition schema, JavaWriter ignore) {
        generateJooqBaseDao(schema);
    }
    protected void generateJooqBaseDao(SchemaDefinition schema) {
        Mode mode = Mode.CORE_JOOQBASEDAO;
        JavaWriter out = newJavaWriter(getFile(schema,mode));
        log.info("generateJooqBaseDao", out.file().getName());
        out.ref(BaseDaoImpl.class);
        out.ref(BaseDao.class);
        out.ref(BaseEntity.class);
        out.ref(Table.class);
        out.ref(UpdatableRecord.class);
        printPackage(out, schema, mode);
        generateSchemaClassJavadoc(schema, out);
        final String className = getStrategy().getJavaClassName(schema, mode);
        out.println("%sabstract class %s<R extends %s<R>,E extends %s<PK>,PK extends Number> extends %s<R, E, PK> implements %s<PK,E> {",
                PUBLIC, className,UpdatableRecord.class.getSimpleName(), BaseEntity.class.getSimpleName(), BaseDaoImpl.class.getSimpleName(), BaseDao.class.getSimpleName());

        // Default constructor
        out.println("protected %s(%s<R> table, Class<E> type) {", className,Table.class.getSimpleName());
        out.println("super(table, type);");
        out.println("}");
        //PostConstruct
        out.ref(CodeGenTool.CFG.getPackageCore() + "." + MIDDLEWARE_DIR + "." + CodeGenTool.MIDDLEWARE_HOLDER);
        out.println("@%s", out.ref("jakarta.annotation.PostConstruct"));
        out.println("public void init() {");
        // MiddlewareHolder
        out.println("setConfiguration(%s.getDSLContext(%s.DATASOURCE_%s).configuration());", CodeGenTool.MIDDLEWARE_HOLDER,CodeGenTool.MIDDLEWARE_HOLDER,schema.getName().toUpperCase());
        out.println("}");


        out.println("}");
        closeJavaWriter(out);
    }

    @Override
    protected File getFile(Definition definition, Mode mode) {
        //原有实现
//        String dir = getTargetDirectory();
//        String pkg =getStrategy().getJavaPackageName(definition, mode).replaceAll("\\.", "/");
//        return new File(dir + "/" + pkg, getStrategy().getFileName(definition, mode));
        String dir = getTargetDirectory();
        if (definition instanceof TableDefinition) {
            if(mode.name().startsWith("CLIENT_")) {
                dir = dir.replace("-core","-client");
            }
            if(mode.name().startsWith("SERVER_")) {
                dir = dir.replace("-core","-server");
            }
            if(mode.equals(Mode.CORE_TEST) || mode.equals(Mode.SERVER_TEST)) {
                dir = dir.replace(TARGET_DIR,"src/test/generated");
            }
            if(mode.equals(Mode.CORE_XML_GENE) || mode.equals(Mode.CORE_XML_MANUAL)) {
                dir = dir.replace(TARGET_DIR,"src/main/resources/mybatis");
                TableDefinition table = (TableDefinition)definition;
                String parentFilePath = dir + "/manual";
                if(mode.equals(Mode.CORE_XML_GENE)) {
                    parentFilePath = dir + "/generate";
                }
                String fileName = getStrategy().getJavaClassName(table, Mode.DEFAULT) + "Mapper.xml";
                return new File(parentFilePath + "/" + table.getSchema().getName(), fileName);
            }
            if(mode.equals(Mode.CORE_DAOPROXY)) {
                //proxy代码是需要改动的，生成到主目录里
                dir = dir.replace("/generated","/java");
            }
        }
        String pkg =getStrategy().getJavaPackageName(definition, mode).replaceAll("\\.", "/");
        return new File(dir + "/" + pkg, getStrategy().getFileName(definition, mode));
    }
    public List<TemplateColumn> getTemplateColumn(List<ColumnDefinition> cols){
        Mode mode = Mode.POJO;
        JavaWriter out = newJavaWriter(getFile(cols.get(0).getContainer()));
        List<TemplateColumn> results = CollectionUtils.newArrayList();
        for (ColumnDefinition col : cols) {
            final String columnTypeFull = getJavaType(col.getType(resolver(out, mode)), out, mode);
            TemplateColumn c = new TemplateColumn();
            c.setJavaName(getStrategy().getJavaMemberName(col, mode));
            c.setJavaType(out.ref(columnTypeFull));
            c.setSqlType(col.getType().getType());
            c.setSqlName(col.getName());
            c.setComment(col.getComment());
            results.add(c);
        }
        return results;
    }
    public TemplateTable getTemplateTable(TableDefinition definition){
        TemplateTable results = new TemplateTable();
        results.setColumns(getTemplateColumn(filterTypedElements(definition,true)));
        results.setComment(definition.getComment());
        results.setSqlName(definition.getName());
        return results;
    }

    protected Map<String,Object> generateTemplateModel(TableDefinition table) {
        Map<String,Object> modelMap = tableModelMap.get(table.getQualifiedName());
        if(modelMap != null) {
            return modelMap;
        }
        modelMap = MapUtils.newHashMap();
        JavaWriter out = newJavaWriter(getFile(table));
        for (Mode mode : Mode.values()) {
            String fullName = getStrategy().getFullJavaClassName(table, mode);
            modelMap.put(mode.name().toLowerCase()+"NameFull",fullName);
            if(mode.equals(Mode.DEFAULT)) {
                modelMap.put(mode.name().toLowerCase()+"Name",fullName.substring(fullName.lastIndexOf(".") + 1));
            } else {
                modelMap.put(mode.name().toLowerCase()+"Name",out.ref(fullName));
            }
        }
        modelMap.put("table",getTemplateTable(table));
        modelMap.put("idType",out.ref(getPrimaryKeyType(table,out)));
        tableModelMap.put(table.getQualifiedName(),modelMap);
        return modelMap;
    }

    protected String refClassName(TableDefinition table,Mode mode,JavaWriter out) {
        Map<String,Object> modelMap = generateTemplateModel(table);
        String fullName = (String)modelMap.get(mode.name().toLowerCase()+"NameFull");
        out.ref(fullName);
        return (String)modelMap.get(mode.name().toLowerCase()+"Name");
    }
}
