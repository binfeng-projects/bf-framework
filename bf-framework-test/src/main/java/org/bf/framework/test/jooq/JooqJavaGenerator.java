package org.bf.framework.test.jooq;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.test.codegen.CodeGenTool;
import org.bf.framework.test.pojo.TemplateSchema;
import org.jooq.codegen.GeneratorStrategy;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.*;
import org.jooq.tools.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.bf.framework.boot.constant.FrameworkConst.*;
import static org.bf.framework.boot.constant.MiddlewareConst.*;
import static org.bf.framework.test.codegen.CodeGenTool.currentMiddlewareType;

@Slf4j
public class JooqJavaGenerator extends DatasourceJavaGenerator {
    protected static Map<String,TemplateSchema> schemaModelMap = MapUtils.newConcurrentHashMap();
    /**
     * 控制整个流程
     * @param db
     */
    @Override
    public void generate(Database db) {
        super.generate(db); //先走完原有流程
        //走扩展逻辑
        if("elasticsearch".equals(currentMiddlewareType)) {
            elasticsearchConfig();
        }
    }
    @Override
    protected void generateTable(SchemaDefinition schema, TableDefinition table) {
        if("elasticsearch".equals(currentMiddlewareType)) {
            elasticsearchDao(table);
            elasticsearchEntity(table);
        }
    }
    //--------------------------elasticsearch----------------------------------
    protected void elasticsearchEntity(TableDefinition table) {
        String classSuffix = "Entity";
        JavaWriter out = commonTableJavaWriter(table,classSuffix);
        Map<String,Object> modelMap = commonTableModelMap(table,classSuffix);
        out.println(freemarkerUtil.renderTemplate("es_entity.java",modelMap));
        closeJavaWriter(out);
    }
    protected void elasticsearchDao(TableDefinition table) {
        String classSuffix = "Repository";
        JavaWriter out = commonTableJavaWriter(table,classSuffix);
        Map<String,Object> modelMap = commonTableModelMap(table,classSuffix,"Entity");
        out.println(freemarkerUtil.renderTemplate("es_dao.java",modelMap));
        closeJavaWriter(out);
    }

    protected void elasticsearchConfig() {
        String subDir = "config";
        JavaWriter out = commonSchemaJavaWriter(subDir);
        out.ref("");
        Map<String,Object> modelMap = commonSchemaModelMap(subDir);
        modelMap.put("middlewareBean",BEAN_ELASTICSEARCHTEMPLATE);
        out.println(freemarkerUtil.renderTemplate("es_config.java",modelMap));
        closeJavaWriter(out);
    }
    //--------------------------公共方法----------------------------------
    @Override
    protected void generateSchema(SchemaDefinition schema) {
        TemplateSchema sc = schemaModelMap.get(schema.getQualifiedName());
        if(sc != null) {
            return;
        }
        sc = new TemplateSchema();
        sc.setSqlName(schema.getName());
        sc.setComment(schema.getComment());
        schemaModelMap.put(schema.getQualifiedName(),sc);

    }
    protected Map<String,Object> commonTableModelMap(TableDefinition table,String classSuffix,String... importClassSuffix) {
        Map<String,Object> modelMap = generateTemplateModel(table);
        modelMap.put("corePackage",CodeGenTool.CFG.getPackageCore());
        modelMap.put("currentPackage",CodeGenTool.CFG.getPackageCore() + "." + currentMiddlewareType + "." + table.getSchema().getName() + "." + classSuffix.toLowerCase());
        modelMap.put("middlewareType", currentMiddlewareType);
        modelMap.put("classSuffix", classSuffix);
        final String className = getStrategy().getJavaClassName(table, GeneratorStrategy.Mode.DEFAULT);
        if(null != importClassSuffix) {
            List<String> importClass = CollectionUtils.newArrayList();
            modelMap.put("importClass", importClass);
            for (String suffix: importClassSuffix) {
                importClass.add(CodeGenTool.CFG.getPackageCore() + "." + currentMiddlewareType + "." + table.getSchema().getName() + "." + suffix.toLowerCase() + "." + className + suffix);
            }
        }
        return modelMap;
    }
    protected JavaWriter commonTableJavaWriter(TableDefinition table,String classSuffix) {
        GeneratorStrategy.Mode mode = GeneratorStrategy.Mode.DEFAULT;
        final String className = getStrategy().getJavaClassName(table, mode);
        JavaWriter out = getWriter(currentMiddlewareType + "/" + table.getSchema().getName() + "/" + classSuffix.toLowerCase(),className + classSuffix +".java");
        return out;
    }
    protected Map<String,Object> commonSchemaModelMap(String subDir) {
        Map<String,Object> modelMap = MapUtils.newHashMap();
        modelMap.put("corePackage",CodeGenTool.CFG.getPackageCore());
        modelMap.put("currentPackage",CodeGenTool.CFG.getPackageCore() + "." + subDir);
        modelMap.put("middlewarePrefix",BF + DOT + currentMiddlewareType);
        modelMap.put("middlewareType", currentMiddlewareType);
        modelMap.put("schemas",schemaModelMap.values());
        return modelMap;
    }
    protected JavaWriter commonSchemaJavaWriter(String subDir) {
        JavaWriter out = getWriter(subDir, StringUtils.toUC(currentMiddlewareType) + "Configuration.java");
        return out;
    }
    protected JavaWriter getWriter(String subDir,String fileName) {
        //原有实现
        return newJavaWriter(new File(getTargetDirectory() + "/" + getCorePackageDir() + "/" + subDir, fileName));
    }

    protected String getCorePackageDir() {
        //原有实现
        return CodeGenTool.CFG.getPackageCore().replaceAll("\\.", "/");
    }
    //--------------------------分割线以上是覆盖系统并自定义，以下是为了屏蔽系统的生成。----------------------------------
    @Override
    protected void generateRecord(TableDefinition table) {
    }
    @Override
    protected void generateDao(TableDefinition table) {
    }
    @Override
    protected void generateSchemaClassFooter(SchemaDefinition schema, JavaWriter ignore) {
    }
    @Override
    protected void generateCatalog(CatalogDefinition catalog) {
    }
}
