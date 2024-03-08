package org.bf.framework.test.jooq;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.autoconfigure.jooq.BaseDaoImpl;
import org.bf.framework.autoconfigure.jooq.JooqAutoConfig;
import org.bf.framework.autoconfigure.mybatis.MyBatisBaseDao;
import org.bf.framework.autoconfigure.mybatis.MybatisAutoConfig;
import org.bf.framework.boot.base.BaseConvert;
import org.bf.framework.boot.base.BaseDaoProxy;
import org.bf.framework.boot.base.PermCheck;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.base.BaseDTO;
import org.bf.framework.common.base.BaseDao;
import org.bf.framework.common.base.BaseEntity;
import org.bf.framework.common.result.PageResult;
import org.bf.framework.common.result.Result;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.test.base.BaseProxyTest;
import org.bf.framework.test.codegen.CodeGenTool;
import org.bf.framework.test.pojo.TemplateColumn;
import org.bf.framework.test.pojo.TemplateSchema;
import org.bf.framework.test.pojo.TemplateTable;
import org.jooq.Condition;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.impl.DSL;
import org.jooq.meta.*;
import org.jooq.tools.StringUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bf.framework.boot.constant.FrameworkConst.BF;
import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.BEAN_ELASTICSEARCHTEMPLATE;
import static org.bf.framework.boot.constant.MiddlewareConst.BEAN_SQLSESSIONFACTORY;
import static org.bf.framework.test.codegen.CodeGenTool.*;

@Slf4j
public class JooqJavaGenerator extends JavaGenerator {
    public static Set<String> ignoreFileds = CollectionUtils.newHashSet("id", "version", "created_at", "updated_at");

    public static final String PUBLIC = "public ";

    protected static Map<String, TemplateSchema> schemaModelMap = MapUtils.newConcurrentHashMap();
    protected static Map<String, Map<String, Object>> tableModelMap = MapUtils.newConcurrentHashMap();

    /**
     * 控制整个流程
     */
    @Override
    public void generate(Database db) {
        super.generate(db); //先走完原有流程
        //走扩展逻辑
        if ("elasticsearch".equals(currentMiddlewareType)) {
            genElasticsearchConfig();
        }
        else if ("datasource".equals(currentMiddlewareType)) {
            if (SpringUtil.getBean(MybatisAutoConfig.class) != null) {
                genMybatisConfig();
            }
        }
    }

    @Override
    protected void generateTable(SchemaDefinition schema, TableDefinition table) {
        if ("elasticsearch".equals(currentMiddlewareType)) {
            genElasticsearchDao(table);
            genElasticsearchEntity(table);
        } else if("datasource".equals(currentMiddlewareType)) {
            super.generateInterface(table);
            genPojo(table);
            genCoreInterface(table);
            genCoreConvert(table);
            genCoreDaoProxy(table);
            genCoreTest(table);
            genServerCtl(table);
            genServerRpc(table);
            genClientDto(table);
            genClientRpc(table);
            if (SpringUtil.getBean(JooqAutoConfig.class) != null) {
                genJooqDao(table);
                super.generateTable(schema,table);
                super.generateRecord(table);
            } else if (SpringUtil.getBean(MybatisAutoConfig.class) != null) {
                genXmlManual(table);
                genXmlGene(table);
            }
        }
    }

    @Override
    protected void generateSchema(SchemaDefinition schema) {
        TemplateSchema sc = schemaModelMap.get(schema.getQualifiedName());
        if (sc != null) {
            return;
        }
        sc = new TemplateSchema();
        sc.setSqlName(schema.getName());
        sc.setComment(schema.getComment());
        schemaModelMap.put(schema.getQualifiedName(), sc);
        if ("datasource".equals(currentMiddlewareType)) {
            if (SpringUtil.getBean(JooqAutoConfig.class) != null) {
                super.generateSchema(schema);
                genJooqBaseDao(schema);
            } else if (SpringUtil.getBean(MybatisAutoConfig.class) != null) {

            }
        }
    }

    @Override
    protected void generateCatalog(CatalogDefinition catalog) {
        if ("datasource".equals(currentMiddlewareType)) {
            if (SpringUtil.getBean(JooqAutoConfig.class) != null) {
                super.generateCatalog(catalog);
            } else if (SpringUtil.getBean(MybatisAutoConfig.class) != null) {

            }
        }
    }
    //--------------------------公共方法----------------------------------
    public List<TemplateColumn> getTemplateColumn(List<ColumnDefinition> cols) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.POJO;
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

    public TemplateTable getTemplateTable(TableDefinition definition) {
        TemplateTable results = new TemplateTable();
        results.setColumns(getTemplateColumn(filterTypedElements(definition, true)));
        results.setComment(definition.getComment());
        results.setSqlName(definition.getName());
        return results;
    }

    protected Map<String, Object> generateTemplateModel(TableDefinition table) {
        Map<String, Object> modelMap = tableModelMap.get(table.getQualifiedName());
        if (modelMap != null) {
            return modelMap;
        }
        modelMap = MapUtils.newHashMap();
        JavaWriter out = newJavaWriter(getFile(table));
        for (GeneratorStrategy.Mode mode : GeneratorStrategy.Mode.values()) {
            String fullName = getStrategy().getFullJavaClassName(table, mode);
            modelMap.put(mode.name().toLowerCase() + "NameFull", fullName);
            if (mode.equals(GeneratorStrategy.Mode.DEFAULT)) {
                modelMap.put(mode.name().toLowerCase() + "Name", fullName.substring(fullName.lastIndexOf(".") + 1));
            } else {
                modelMap.put(mode.name().toLowerCase() + "Name", out.ref(fullName));
            }
        }
        modelMap.put("table", getTemplateTable(table));
        modelMap.put("idType", out.ref(getPrimaryKeyType(table, out)));
        tableModelMap.put(table.getQualifiedName(), modelMap);
        return modelMap;
    }

    protected String refClassName(TableDefinition table, GeneratorStrategy.Mode mode, JavaWriter out) {
        Map<String, Object> modelMap = generateTemplateModel(table);
        String fullName = (String) modelMap.get(mode.name().toLowerCase() + "NameFull");
        out.ref(fullName);
        return (String) modelMap.get(mode.name().toLowerCase() + "Name");
    }

    protected String getPrimaryKeyType(TableDefinition table, JavaWriter out) {
        UniqueKeyDefinition key = table.getPrimaryKey();
        if (key == null) {
            return "String";
        }
        List<ColumnDefinition> keyColumns = key.getKeyColumns();
        String result = getJavaType(keyColumns.get(0).getType(resolver(out)), out, GeneratorStrategy.Mode.POJO);
        if (org.bf.framework.common.util.StringUtils.isBlank(result)) {
            result = "String";
        }
        return result;
    }

    public static List<ColumnDefinition> filterTypedElements(TableDefinition definition, boolean filter) {
        List<ColumnDefinition> colDefins = definition.getColumns();
        if (filter) {
            return colDefins.stream().filter(def -> !ignoreFileds.contains(def.getName())).toList();
        }
        return colDefins;
    }

    protected Map<String, Object> commonTableModelMap(TableDefinition table, String classSuffix, String... importClassSuffix) {
        Map<String, Object> modelMap = generateTemplateModel(table);
        modelMap.put("corePackage", CodeGenTool.CFG.getPackageCore());
        modelMap.put("currentPackage", CodeGenTool.CFG.getPackageCore() + "." + currentMiddlewareType + "." + table.getSchema().getName() + "." + classSuffix.toLowerCase());
        modelMap.put("middlewareType", currentMiddlewareType);
        modelMap.put("classSuffix", classSuffix);
        final String className = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.DEFAULT);
        if (null != importClassSuffix) {
            List<String> importClass = CollectionUtils.newArrayList();
            modelMap.put("importClass", importClass);
            for (String suffix : importClassSuffix) {
                importClass.add(CodeGenTool.CFG.getPackageCore() + "." + currentMiddlewareType + "." + table.getSchema().getName() + "." + suffix.toLowerCase() + "." + className + suffix);
            }
        }
        return modelMap;
    }

    protected JavaWriter commonTableJavaWriter(TableDefinition table, String classSuffix) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.DEFAULT;
        final String className = getStrategy().getJavaClassName(table, mode);
        JavaWriter out = getWriter(currentMiddlewareType + "/" + table.getSchema().getName() + "/" + classSuffix.toLowerCase(), className + classSuffix + ".java");
        return out;
    }

    protected Map<String, Object> commonSchemaModelMap(String subDir) {
        Map<String, Object> modelMap = MapUtils.newHashMap();
        modelMap.put("corePackage", CodeGenTool.CFG.getPackageCore());
        modelMap.put("currentPackage", CodeGenTool.CFG.getPackageCore() + "." + subDir);
        modelMap.put("middlewarePrefix", BF + DOT + currentMiddlewareType);
        modelMap.put("middlewareType", currentMiddlewareType);
        modelMap.put("schemas", schemaModelMap.values());
        return modelMap;
    }

    protected JavaWriter commonSchemaJavaWriter(String subDir,String fileNameOrNull) {
        String fileName = StringUtils.isBlank(fileNameOrNull) ? StringUtils.toUC(currentMiddlewareType) + "Configuration.java" : fileNameOrNull;
        JavaWriter out = getWriter(subDir,fileName);
        return out;
    }

    protected JavaWriter getWriter(String subDir, String fileName) {
        //原有实现
        return newJavaWriter(new File(getTargetDirectory() + "/" + getCorePackageDir() + "/" + subDir, fileName));
    }

    protected String getCorePackageDir() {
        //原有实现
        return CodeGenTool.CFG.getPackageCore().replaceAll("\\.", "/");
    }

    @Override
    protected File getFile(Definition definition, GeneratorStrategy.Mode mode) {
        //原有实现
//        String dir = getTargetDirectory();
//        String pkg =getStrategy().getJavaPackageName(definition, mode).replaceAll("\\.", "/");
//        return new File(dir + "/" + pkg, getStrategy().getFileName(definition, mode));
        String dir = getTargetDirectory();
        if (definition instanceof TableDefinition) {
            if (mode.name().startsWith("CLIENT_")) {
                dir = dir.replace("-core", "-client");
            }
            if (mode.name().startsWith("SERVER_")) {
                dir = dir.replace("-core", "-server");
            }
            if (mode.equals(GeneratorStrategy.Mode.CORE_TEST) || mode.equals(GeneratorStrategy.Mode.SERVER_TEST)) {
                dir = dir.replace(TARGET_DIR, "src/test/generated");
            }
            if (mode.equals(GeneratorStrategy.Mode.CORE_XML_GENE) || mode.equals(GeneratorStrategy.Mode.CORE_XML_MANUAL)) {
                dir = dir.replace(TARGET_DIR, "src/main/resources/mybatis");
                TableDefinition table = (TableDefinition) definition;
                String parentFilePath = dir + "/manual";
                if (mode.equals(GeneratorStrategy.Mode.CORE_XML_GENE)) {
                    parentFilePath = dir + "/generate";
                }
                String fileName = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.DEFAULT) + "Mapper.xml";
                return new File(parentFilePath + "/" + table.getSchema().getName(), fileName);
            }
            if (mode.equals(GeneratorStrategy.Mode.CORE_DAOPROXY)) {
                //proxy代码是需要改动的，生成到主目录里
                dir = dir.replace("/generated", "/java");
            }
        }
        String pkg = getStrategy().getJavaPackageName(definition, mode).replaceAll("\\.", "/");
        return new File(dir + "/" + pkg, getStrategy().getFileName(definition, mode));
    }

    //    ---------------------------所有具体生成代码逻辑----------------------------
//--------------------------elasticsearch----------------------------------
    protected void genElasticsearchEntity(TableDefinition table) {
        String classSuffix = "Entity";
        JavaWriter out = commonTableJavaWriter(table, classSuffix);
        Map<String, Object> modelMap = commonTableModelMap(table, classSuffix);
        out.println(freemarkerUtil.renderTemplate("es_entity.java", modelMap));
        closeJavaWriter(out);
    }

    protected void genElasticsearchDao(TableDefinition table) {
        String classSuffix = "Repository";
        JavaWriter out = commonTableJavaWriter(table, classSuffix);
        Map<String, Object> modelMap = commonTableModelMap(table, classSuffix, "Entity");
        out.println(freemarkerUtil.renderTemplate("es_dao.java", modelMap));
        closeJavaWriter(out);
    }

    protected void genElasticsearchConfig() {
        String subDir = "config";
        JavaWriter out = commonSchemaJavaWriter(subDir,null);
        out.ref("");
        Map<String, Object> modelMap = commonSchemaModelMap(subDir);
        modelMap.put("middlewareBean", BEAN_ELASTICSEARCHTEMPLATE);
        out.println(freemarkerUtil.renderTemplate("es_config.java", modelMap));
        closeJavaWriter(out);
    }

    //--------------------------jooq----------------------------------
    protected void genJooqBaseDao(SchemaDefinition schema) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_JOOQBASEDAO;
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
    protected void genJooqDao(TableDefinition table) {
        JavaWriter out = newJavaWriter(getFile(table, GeneratorStrategy.Mode.DAO));
        final String daoName = refClassName(table, GeneratorStrategy.Mode.DAO,out);
        final String tableRecord = refClassName(table, GeneratorStrategy.Mode.RECORD,out);
        final String fullTableName = getStrategy().getFullJavaClassName(table);
        final String tableIdentifier = table.getName().toUpperCase();

        String baseDaoInterface = refClassName(table, GeneratorStrategy.Mode.CORE_INTERFACE,out);
        String entityName = refClassName(table, GeneratorStrategy.Mode.POJO,out);
        String tType = out.ref(getPrimaryKeyType(table,out));
        String baseDaoJavaName = out.ref(getStrategy().getFullJavaClassName(table.getSchema(), GeneratorStrategy.Mode.CORE_JOOQBASEDAO));;
        printPackage(out, table, GeneratorStrategy.Mode.DAO);
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
    //--------------------------mybatis----------------------------------
    protected void genMybatisConfig() {
        String subDir = "config";
        JavaWriter out = commonSchemaJavaWriter(subDir,"MybatisConfiguration.java");
        out.ref("");
        Map<String, Object> modelMap = commonSchemaModelMap(subDir);
        modelMap.put("middlewareBean", BEAN_SQLSESSIONFACTORY);
        out.println(freemarkerUtil.renderTemplate("mybatis_config.java", modelMap));
        closeJavaWriter(out);
    }
    protected void genXmlManual(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_XML_MANUAL;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        String daoFullName = getStrategy().getFullJavaClassName(table, GeneratorStrategy.Mode.CORE_INTERFACE);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        out.println("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >");
        out.println("<mapper namespace=\"%s\" >", daoFullName);
        out.println();
        out.println();
        out.println();
        out.println("</mapper>");
        closeJavaWriter(out);
    }
    protected void genXmlGene(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_XML_GENE;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.println(freemarkerUtil.renderTemplate("generate.xml",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    //--------------------------通用crud----------------------------------
    protected void genPojo(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.POJO;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseEntity.class);
        final String className = getStrategy().getJavaClassName(table, mode);
        final String interfaceName = out.ref(getStrategy().getFullJavaClassName(table, GeneratorStrategy.Mode.INTERFACE));
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
            genPojoGetter(column, i, out);
            out.println();
            genPojoSetter(column, i, out);
        }
//        generatePojoEqualsAndHashCode(table, out);
//        generatePojoToString(table, out);
        printFromAndInto(out, table);
        //into
        out.overrideInherit();
        out.println("%s<E extends %s> E into(E into) {", new Object[]{PUBLIC, interfaceName});
        out.println("into.from(this);");
        out.println("return into;");
        out.println("}");
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
    public void genPojoGetter(TypedElementDefinition<?> column, @SuppressWarnings("unused") int index, JavaWriter out) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.POJO;
        final String columnTypeFull = getJavaType(column.getType(resolver(out, mode)), out, mode);
        final String columnType = out.ref(columnTypeFull);
        final String columnGetter = getStrategy().getJavaGetterName(column, mode);
        final String columnMember = getStrategy().getJavaMemberName(column, mode);
        out.println("%s%s %s() {", PUBLIC, columnType, columnGetter);
        out.println("return this.%s;", columnMember);
        out.println("}");
    }
    public void genPojoSetter(TypedElementDefinition<?> column, @SuppressWarnings("unused") int index, JavaWriter out) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.POJO;
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
    protected void genServerRpc(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.SERVER_RPC;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(Slf4j.class);
        out.ref(Autowired.class);
        out.ref("org.apache.dubbo.config.annotation.DubboService");
        out.ref(PageResult.class);
        out.ref(Result.class);
        refClassName(table, GeneratorStrategy.Mode.CORE_DAOPROXY,out);
        refClassName(table, GeneratorStrategy.Mode.CLIENT_DTO,out);
        refClassName(table, GeneratorStrategy.Mode.CLIENT_RPC,out);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("rpc_impl.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void genClientRpc(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CLIENT_RPC;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(PageResult.class);
        out.ref(Result.class);
        refClassName(table, GeneratorStrategy.Mode.CLIENT_DTO,out);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("rpc_api.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void genServerCtl(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.SERVER_CTL;
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
        refClassName(table, GeneratorStrategy.Mode.CLIENT_DTO,out);
        refClassName(table, GeneratorStrategy.Mode.CORE_DAOPROXY,out);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("ctl.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void genCoreDaoProxy(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_DAOPROXY;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseDaoProxy.class);
        String tType = out.ref(getPrimaryKeyType(table,out));
        String dtoType = refClassName(table, GeneratorStrategy.Mode.CLIENT_DTO,out);
        String entityType = refClassName(table, GeneratorStrategy.Mode.POJO,out);
        printPackage(out, table, mode);
        final String className = getStrategy().getJavaClassName(table, mode);
        out.println("@%s", out.ref(Component.class));
        out.println("%sclass %s extends %s<%s,%s,%s> {",
                PUBLIC, className, BaseDaoProxy.class.getSimpleName(), tType,dtoType, entityType);
        out.println("}");
        closeJavaWriter(out);
    }
    protected void genCoreTest(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_TEST;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseProxyTest.class);
        refClassName(table, GeneratorStrategy.Mode.CLIENT_DTO,out);
        refClassName(table, GeneratorStrategy.Mode.POJO,out);
        out.ref(Test.class);
        printPackage(out, table, mode);
        out.println(freemarkerUtil.renderTemplate("coretest.java",generateTemplateModel(table)));
        closeJavaWriter(out);
    }
    protected void genCoreInterface(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_INTERFACE;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        Class<?> interfaceClass =  null;
        if (SpringUtil.getBean(MybatisAutoConfig.class) != null) {
            interfaceClass = MyBatisBaseDao.class;
        } else {
            interfaceClass = BaseDao.class;
        }
        out.ref(interfaceClass);
        String entityType = refClassName(table, GeneratorStrategy.Mode.POJO,out);
        String tType = out.ref(getPrimaryKeyType(table,out));
        printPackage(out, table, mode);
        generateDaoClassJavadoc(table, out);
//        out.println("@%s", out.ref("org.springframework.stereotype.Repository"));
        final String className = getStrategy().getJavaClassName(table, mode);
        out.println("%sinterface %s extends %s<%s, %s> {",
                PUBLIC, className, interfaceClass.getSimpleName(), tType, entityType);

        out.println("}");
        closeJavaWriter(out);
    }
    protected void genCoreConvert(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CORE_CONVERT;
        JavaWriter out = newJavaWriter(getFile(table,mode));
        out.ref(BaseConvert.class);
        String tType = out.ref(getPrimaryKeyType(table,out));
        String dtoType = refClassName(table, GeneratorStrategy.Mode.CLIENT_DTO,out);
        String entityType = refClassName(table, GeneratorStrategy.Mode.POJO,out);
        printPackage(out, table, mode);
        final String className = getStrategy().getJavaClassName(table, mode);
        out.println("@%s(componentModel = \"spring\")", out.ref(Mapper.class));
        out.println("%sinterface %s extends %s<%s,%s,%s> {",
                PUBLIC, className, BaseConvert.class.getSimpleName(), tType,dtoType, entityType);
        out.println("}");
        closeJavaWriter(out);
    }
    protected void genClientDto(TableDefinition table) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.CLIENT_DTO;
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
    /*--------------------------以下是为了屏蔽系统的生成。----------------------------------*/
    @Override
    protected void generatePojo(TableDefinition table) {

    }
    @Override
    protected void generateRecord(TableDefinition table) {
    }

    @Override
    protected void generateDao(TableDefinition table) {
    }

}
