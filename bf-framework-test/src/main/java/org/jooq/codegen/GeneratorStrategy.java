package org.jooq.codegen;

import org.jooq.meta.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public interface GeneratorStrategy {
    String getTargetDirectory();

    void setTargetDirectory(String var1);

    String getTargetPackage();

    void setTargetPackage(String var1);

    Locale getTargetLocale();

    void setTargetLocale(Locale var1);

    AbstractGenerator.Language getTargetLanguage();

    void setTargetLanguage(AbstractGenerator.Language var1);

    void setInstanceFields(boolean var1);

    boolean getInstanceFields();

    void setJavaBeansGettersAndSetters(boolean var1);

    boolean getJavaBeansGettersAndSetters();

    String getJavaIdentifier(Definition var1);

    List<String> getJavaIdentifiers(Collection<? extends Definition> var1);

    List<String> getJavaIdentifiers(Definition... var1);

    String getFullJavaIdentifier(Definition var1);

    List<String> getFullJavaIdentifiers(Collection<? extends Definition> var1);

    List<String> getFullJavaIdentifiers(Definition... var1);

    String getJavaSetterName(Definition var1);

    String getJavaSetterName(Definition var1, Mode var2);

    String getJavaGetterName(Definition var1);

    String getJavaGetterName(Definition var1, Mode var2);

    String getJavaMethodName(Definition var1);

    String getJavaMethodName(Definition var1, Mode var2);

    String getGlobalReferencesJavaClassExtends(Definition var1, Class<? extends Definition> var2);

    String getJavaClassExtends(Definition var1);

    String getJavaClassExtends(Definition var1, Mode var2);

    List<String> getGlobalReferencesJavaClassImplements(Definition var1, Class<? extends Definition> var2);

    List<String> getJavaClassImplements(Definition var1);

    List<String> getJavaClassImplements(Definition var1, Mode var2);

    String getGlobalReferencesJavaClassName(Definition var1, Class<? extends Definition> var2);

    String getJavaClassName(Definition var1);

    String getJavaClassName(Definition var1, Mode var2);

    String getGlobalReferencesJavaPackageName(Definition var1, Class<? extends Definition> var2);

    String getJavaPackageName(Definition var1);

    String getJavaPackageName(Definition var1, Mode var2);

    String getJavaMemberName(Definition var1);

    String getJavaMemberName(Definition var1, Mode var2);

    String getGlobalReferencesFullJavaClassName(Definition var1, Class<? extends Definition> var2);

    String getFullJavaClassName(Definition var1);

    String getFullJavaClassName(Definition var1, Mode var2);

    String getGlobalReferencesFileName(Definition var1, Class<? extends Definition> var2);

    String getFileName(Definition var1);

    String getFileName(Definition var1, Mode var2);

    File getFileRoot();

    File getGlobalReferencesFile(Definition var1, Class<? extends Definition> var2);

    File getFile(Definition var1);

    File getFile(Definition var1, Mode var2);

    File getFile(String var1);

    String getGlobalReferencesFileHeader(Definition var1, Class<? extends Definition> var2);

    String getFileHeader(Definition var1);

    String getFileHeader(Definition var1, Mode var2);

    String getOverloadSuffix(Definition var1, Mode var2, String var3);

    /**
     * The "mode" by which an artefact should be named
     */
    enum Mode {

        /**
         * The default mode. This is used when any {@link Definition}'s meta
         * type is being rendered.
         */
        DEFAULT,

        /**
         * The record mode. This is used when a {@link TableDefinition} or a
         * {@link UDTDefinition}'s record class is being rendered.
         */
        RECORD,

        /**
         * The pojo mode. This is used when a {@link TableDefinition}'s pojo
         * class is being rendered
         */
        POJO,

        /**
         * the interface mode. This is used when a {@link TableDefinition}'s
         * interface is being rendered
         */
        INTERFACE,

        /**
         * The dao mode. This is used when a {@link TableDefinition}'s dao class
         * is being rendered
         */
        DAO,

        /**
         * The synthetic dao mode. This is used when a
         * {@link SyntheticDaoDefinition}'s dao class is being rendered
         */
        SYNTHETIC_DAO,

        /**
         * The enum mode. This is used when a {@link EnumDefinition}'s class is
         * being rendered
         */
        ENUM,

        /**
         * The domain mode. This is used when a {@link DomainDefinition}'s class
         * is being rendered
         */
        DOMAIN,
        /**
         * 自定义,core模块Dao的interface。因为是接口，可以作为mybatis的实现接口，用jooq的时候也可以生成出来，放着不用
         */
        CORE_INTERFACE,
        /**
         * 自定义,core模块jooqBaseDao
         */
        CORE_JOOQBASEDAO,
        /**
         * 自定义的,core模块mybatis自动生成的xml
         */
        CORE_XML_GENE,
        /**
         * 自定义的,core模块mybatis手动写的xml
         */
        CORE_XML_MANUAL,
        /**
         * 自定义,core模块convert
         */
        CORE_CONVERT,
        /**
         * 自定义,core模块dao的代理
         */
        CORE_DAOPROXY,
        /**
         * 自定义的,core模块单元测试
         */
        CORE_TEST,
        /**
         * 自定义的,server模块单元测试
         */
        SERVER_TEST,
        /**
         * 自定义,server模块Controller
         */
        SERVER_CTL,
        /**
         * 自定义,server模块RPC的实现类，一般是dubbo,rpc的实现类
         */
        SERVER_RPC,
        /**
         * 自定义,client模块的RPC
         */
        CLIENT_RPC,
        /**
         * 自定义,client模块的DTO
         */
        CLIENT_DTO
    }
}
