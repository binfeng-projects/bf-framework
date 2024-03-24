package org.bf.framework.test.codegen;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.Middleware;
import org.bf.framework.boot.util.FreemarkerUtil;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.boot.util.YamlUtil;
import org.bf.framework.common.util.*;
import org.bf.framework.test.jooq.JooqJavaGenerator;
import org.bf.framework.test.jooq.JooqJavaStrategy;
import org.jooq.codegen.GenerationTool;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

import static org.bf.framework.boot.constant.FrameworkConst.BF;
import static org.bf.framework.boot.constant.FrameworkConst.DOT;
import static org.bf.framework.boot.constant.MiddlewareConst.*;

/**
 * 请确保在springboot环境中运行，最佳实践是单元测试spring-boot-test中运行
 * 会读取所有你配置的datasource,一键生成crud代码。
 * 因为会依赖spring容器，也相当于帮你验证了你的spring配置（datasource部分）是否正确
 */
public class CodeGenTool extends JavaGenerator {
    public static GenConfig CFG;
    //当前正在执行哪个中间件类型的代码自动生成,默认datasource
    public static String currentMiddlewareType = PREFIX_DATASOURCE;
    private JavaWriter middlewareHolderOut;
    public static FreemarkerUtil freemarkerUtil;
    static {
        try {
            freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32);
            cfg.setSettings(new ClassPathResource("freemarker.properties").getInputStream());
            cfg.setEncoding(Locale.SIMPLIFIED_CHINESE, "UTF-8");
            cfg.setClassForTemplateLoading(JooqJavaGenerator.class, "/templates");
            cfg.setDefaultEncoding("UTF-8");
            freemarkerUtil = new FreemarkerUtil(cfg);
        } catch (Exception e) {

        }
    }
    public static void initProject(String workspace,String appName,String corePackage,String middlewareGroupId,String middlewareArtifactId){
        if(StringUtils.isBlank(workspace) || StringUtils.isBlank(appName) || StringUtils.isBlank(corePackage)) {
            throw new RuntimeException("appName and workspace path cannot empty");
        }
        String basePath = workspace + "/" + appName ;
        Map<String, Object> model = new HashMap<>();
        if (org.jooq.tools.StringUtils.isBlank(middlewareGroupId)) {
            middlewareGroupId = "com.bf.middleware";
        }
        if (org.jooq.tools.StringUtils.isBlank(middlewareArtifactId)) {
            middlewareArtifactId = "binfeng-middleware";
        }
        model.put("middlewareGroupId",middlewareGroupId);
        model.put("middlewareArtifactId",middlewareArtifactId);
        model.put("corePackage",corePackage);
        String groupId = corePackage.substring(0,corePackage.lastIndexOf("."));
        model.put("groupId",groupId);
        model.put("appName",appName);
        String[] rootPackage = groupId.split("\\.");
        model.put("rootPackage",rootPackage[0] + "." + rootPackage[1]);
        //root
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/root_pom.xml",model),basePath, "pom.xml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/.gitignore",model),basePath, ".gitignore");
        //client
        String clientPath = basePath + "/" + appName + "-client";
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/client_pom.xml",model),clientPath, "pom.xml");
        //core
        String corePath = basePath + "/" + appName + "-core";
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_pom.xml",model),corePath, "pom.xml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_constant.java",model),corePath + "/src/main/java/" + corePackage.replace(".","/"), "Constant.java");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/spring.factories",model),corePath + "/src/main/resources/META-INF","spring.factories");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core.yml",model),corePath + "/src/main/resources", appName + "-core.yml");
        model.put("env","dev");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_env.yml",model),corePath + "/src/main/resources", appName + "-core-dev.yml");
        model.put("env","test");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_env.yml",model),corePath + "/src/main/resources", appName + "-core-test.yml");
        model.put("env","pre");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_env.yml",model),corePath + "/src/main/resources", appName + "-core-pre.yml");
        model.put("env","prod");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_env.yml",model),corePath + "/src/main/resources", appName + "-core-prod.yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_test_application.yml",model),corePath + "/src/test/resources", "application.yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_test_gencode.java",model),corePath + "/src/test/java/" + corePackage.replace(".","/") + "/test", "TestGenCode.java");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/core_test_simplejunit.java",model),corePath + "/src/test/java/" + corePackage.replace(".","/"), "SimpleJunitTest.java");

        //server
        String serverPath = basePath + "/" + appName + "-server";
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_pom.xml",model),serverPath, "pom.xml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_appmain.java",model),serverPath + "/src/main/java/" + groupId.replace(".","/"), "AppMain.java");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server.yml",model),serverPath + "/src/main/resources", appName + ".yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_application.yml",model),serverPath + "/src/main/resources", "application.yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_application_env.yml",model),serverPath + "/src/main/resources", "application-dev.yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_application_env.yml",model),serverPath + "/src/main/resources", "application-test.yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_application_env.yml",model),serverPath + "/src/main/resources", "application-pre.yml");
        IOUtils.writeFile(freemarkerUtil.renderTemplate("init/server_application_env.yml",model),serverPath + "/src/main/resources", "application-prod.yml");
    }
    public CodeGenTool(GenConfig cfg) {
        if(StringUtils.isBlank(cfg.getPackageCore())) {
            throw new RuntimeException("packageCore cannot empty");
        }
        CFG = cfg;
        setStrategy(new JooqJavaStrategy());
        try {
            String base = new File(".").getAbsolutePath();
            setTargetDirectory(new File(base, TARGET_DIR).getCanonicalPath());
        } catch (Exception e) {

        }
        if(CollectionUtils.isEmpty(CFG.getMiddlewarePrefix())){
            CFG.setMiddlewarePrefix(ALL_MIDDLEWARE_TYPE);
        }
    }
    /**
     * 以下都是不允许覆盖的
     */
    public static final String TARGET_DIR ="src/main/generated";
    public static final String MYSQL_DRIVER ="com.mysql.cj.jdbc.Driver";
    public static final String MYSQL_META ="org.jooq.meta.mysql.MySQLDatabase";
    public static final String STRATEGY_NAME = JooqJavaStrategy.class.getName();
    public static final String MIDDLEWARE_HOLDER ="MiddlewareHolder";
    public static final String MIDDLEWARE_DIR ="proxy";
    private static Map<String,StringBuffer> bufferContext = MapUtils.newHashMap();
    private static Set<Class<?>> clsContext = CollectionUtils.newHashSet();

    public void geneCode(){
        String pkg = CFG.getPackageCore().replaceAll("\\.", "/");
        middlewareHolderOut = newJavaWriter(new File(getTargetDirectory() + "/" + pkg + "/" + MIDDLEWARE_DIR, MIDDLEWARE_HOLDER + ".java"));
        //根据所有使用的中间件生成代码
        List<String> usedMiddlewarePrefix =CollectionUtils.newArrayList();
        StringBuffer bodyBuffer = new StringBuffer();
        for (String middlewarePrefix : CFG.getMiddlewarePrefix()) {
            Map<String, List<Middleware>> middlewareMap = Middleware.getByPrefix(middlewarePrefix);
            if(MapUtils.isEmpty(middlewareMap)) {
                continue;
            }
            usedMiddlewarePrefix.add(middlewarePrefix);
            bodyBuffer.append(genMiddlewareCommon(middlewarePrefix,middlewareMap));
        }
        //public class MiddlewareHolder {
        genMiddlewareHolderBefore(middlewareHolderOut);
        //body
        middlewareHolderOut.println(bodyBuffer.toString());
        // } class结束
        genAfter(middlewareHolderOut);
        closeJavaWriter(middlewareHolderOut);

        if(CollectionUtils.isEmpty(usedMiddlewarePrefix)) {
            return;
        }
        for (String middlewarePrefix : usedMiddlewarePrefix) {
            currentMiddlewareType = getMiddleTypeWithPrefix(middlewarePrefix);
            YamlUtil.parsePrefix(middlewarePrefix,null,configMap -> {
                Map<String, Object> mysqlConfig = null;
                if (PREFIX_DATASOURCE.equals(middlewarePrefix)) {
                    mysqlConfig = configMap;
                } else {
                    String codeGenRef = (String) configMap.get(YamlUtil.CODE_GEN_REF);
                    if (StringUtils.isBlank(codeGenRef)) {
                        return;
                    }
                    mysqlConfig = MapUtils.newHashMap();
                    SpringUtil.bind(PREFIX_DATASOURCE + DOT + codeGenRef, mysqlConfig);
                    if(MapUtils.isEmpty(mysqlConfig)) {
                        return;
                    }
                }
                Configuration cfg = geneJooqConfig(currentMiddlewareType,mysqlConfig);
                if(null != cfg) {
                    try {
                        GenerationTool.generate(cfg);
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            });
        }
    }
    private static boolean printBean(Middleware m) {
        return m.getType() != null && m.getBean() != null && m.getCodeGen() != null && m.getCodeGen();
    }
    private StringBuffer genMiddlewareCommon(String middlewarePrefix,Map<String, List<Middleware>> middlewareMap) {
        String middleType = getMiddleTypeWithPrefix(middlewarePrefix);
        int step = 0;
        StringBuffer bodyBuffer = new StringBuffer();
        for (Map.Entry<String,List<Middleware>> entry : middlewareMap.entrySet()) {
            step++;
            List<Middleware> middlewares = entry.getValue();
            if(step == 1) { //每个中间件第一次回调
                boolean writeHead = false;
                for (int i = 0; i < middlewares.size(); i++) {
                    Middleware m = middlewares.get(i);
                    if(!printBean(m)) {
                        continue;
                    }
                    if(!writeHead) {
                        bodyBuffer.append(String.format("//--------------------------------------------------%s all beans && getter------------------------------------------------------------\n",middleType));
                        writeHead = true;
                    }
                    clsContext.add(m.getType());
                    String className = m.getType().getSimpleName();
                    bodyBuffer.append(String.format("public static %s get%s(String schema) {\n", className,className));
                    bodyBuffer.append(String.format("    return (%s)Middleware.getMiddlewareBean(PREFIX_%s,schema,%s.class);\n", className,middleType.toUpperCase(),className));
                    bodyBuffer.append("}\n");
                }
            }
            boolean writeHead = false;
            for (int i = 0; i < middlewares.size(); i++) {
                Middleware m = middlewares.get(i);
                String schema = m.getSchemaName(); //schema常量无论如何打印
                String middleType_schema = String.format("%s_%s",middleType.toUpperCase(),schema.toUpperCase()).replace(".","_");
                if(m.getCodeGen() == null) { //啥都不生成
                    continue;
                }
                //至少会生成schema
                if(!printBean(m)) {
                    bodyBuffer.append(String.format("//------%s %s -----------\n",middleType,schema));
                    bodyBuffer.append(String.format("public static final String  %s = \"%s\";\n",middleType_schema,schema));
                    continue;
                }
                if(!writeHead) {                      //每个中间件第一次回调
                    if(step == 1) {                      //每个中间件第一次回调
                        bodyBuffer.append(String.format("//-----------------------------------------------%s all beanNames, @Autowired配合@Qualifier 使用-------------------------------\n",middleType));
                    }
                    bodyBuffer.append(String.format("//------%s %s -----------\n",middleType,schema));
                    bodyBuffer.append(String.format("public static final String  %s = \"%s\";\n",middleType_schema,schema));
                    writeHead = true;
                }
                String className = m.getType().getSimpleName();
                String classNameUpper = className.toUpperCase();
                //beanName注释
                bodyBuffer.append("/**\n");
                bodyBuffer.append(String.format(" * %s beanName\n",className));
                bodyBuffer.append(String.format(" * %s.%s_%s\n",middlewarePrefix,schema,className));
                bodyBuffer.append(" */\n");
                bodyBuffer.append(String.format("public static final String BEAN_%s_%s = PREFIX_%s + DOT + %s + BEAN_%s;\n",classNameUpper,schema.toUpperCase().replace(".","_"),middleType.toUpperCase(),middleType_schema,classNameUpper));
            }
        }
        return bodyBuffer;
    }
    private void genMiddlewareHolderBefore(JavaWriter out){
        clsContext.add(Component.class);
        clsContext.add(Slf4j.class);
        clsContext.add(Middleware.class);
        out.println("package %s.%s;",CFG.getPackageCore(),MIDDLEWARE_DIR);
        for (Class<?> c :clsContext) {
            out.println("import %s;",c.getName());
        }
        out.println("import static org.bf.framework.boot.constant.FrameworkConst.*;");
        out.println("import static org.bf.framework.boot.constant.MiddlewareConst.*;");
        out.println("@%s", "Slf4j");
        out.println("@%s", "Component");
        out.println("public class %s {",MIDDLEWARE_HOLDER);

    }
    //    private void genMiddlewareInit(JavaWriter out){
//        out.println("@%s", "PostConstruct");
//        out.println("public void init(){");
//    }
    private void genAfter(JavaWriter out){
        out.println("}");
    }
    private static Configuration geneJooqConfig(String middleWareType,Map<String,Object> configMap){
        try {
            String jdbcUrl = String.valueOf(configMap.get(URL));
            String userName = String.valueOf(configMap.get("username"));
            String password = String.valueOf(configMap.get("password"));
            String includeTable = String.valueOf(configMap.get(YamlUtil.CODE_GEN_INCLUDE));
            String excludeTable = String.valueOf(configMap.get(YamlUtil.CODE_GEN_EXCLUDE));
            String schema = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1).split("\\?")[0];
            ClassPathResource file = null;
            if(SystemUtil.isJdk8()) {
                file = new ClassPathResource("jooq-314.xml");
            } else {
                file = new ClassPathResource("jooq-default.xml");
            }
//        String fileStr = file.getContentAsString(StandardCharsets.UTF_8);
            String packageName = CFG.getPackageCore() + ".jooq.";
            Class<?> generateClass = JooqJavaGenerator.class;
            if("datasource".equals(middleWareType)) {
                packageName = packageName + schema;
            } else {
//                防止冲突，覆盖已经生成的代码
                packageName = packageName + "not_exists";
            }
//            Configuration cfg = JAXB.unmarshal(file.getInputStream(), Configuration.class);
            Configuration cfg = JSON.xmlToBean(new InputStreamReader(file.getInputStream()),Configuration.class);
            cfg.getGenerator().withName(generateClass.getName()).getStrategy().withName(STRATEGY_NAME);
            cfg.getJdbc().withDriver(MYSQL_DRIVER).withUrl(jdbcUrl)
                    .withUser(userName).withPassword(password);
            Database database = cfg.getGenerator().getDatabase().withName(MYSQL_META).withInputSchema(schema);
            if(StringUtils.isNotBlank(includeTable)) {
                database.withIncludes(includeTable).withExcludes(excludeTable);
            }
            if(StringUtils.isNotBlank(excludeTable)) {
                database.withExcludes(excludeTable);
            }
            cfg.getGenerator().getTarget().withDirectory(TARGET_DIR)
                    .withPackageName(packageName);
            return cfg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String getMiddleTypeWithPrefix(String middlewarePrefix){
        return middlewarePrefix.substring((BF + DOT).length());
    }
//    private final Patterns patterns = new Patterns();
//    public final List<String> filterExcludeInclude(List<String> definitions,String e,String i) {
//        List<String> result = filterExcludeInclude(definitions, new String[]{e}, new String[]{i});
//        return result;
//    }
//    protected final List<String>  filterExcludeInclude(List<String> definitions, String[] e, String[] i) {
//        List<String> result = new ArrayList<>();
//        if (i == null || i.length == 0)
//            i = new String[] { ".*" };
//        definitionsLoop: for (String definition : definitions) {
//            if (e != null) {
//                for (String exclude : e) {
//                    if (exclude != null && matches(patterns.pattern(exclude), definition)) {
//                        continue definitionsLoop;
//                    }
//                }
//            }
//            if (i != null) {
//                for (String include : i) {
//                    if (include != null && matches(patterns.pattern(include), definition)) {
//                        result.add(definition);
//                        continue definitionsLoop;
//                    }
//                }
//            }
//        }
//        return result;
//    }
//    final boolean matches(Pattern pattern, String input) {
//        if (pattern == null)
//            return false;
//        return pattern.matcher(input).matches();
//    }
}
