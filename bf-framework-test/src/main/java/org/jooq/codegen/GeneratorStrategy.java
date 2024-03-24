package org.jooq.codegen;

import org.jooq.meta.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public interface GeneratorStrategy {

    // -------------------------------------------------------------------------
    // XXX: Configuration of the strategy
    // -------------------------------------------------------------------------

    /**
     * The target directory
     */
    String getTargetDirectory();

    /**
     * Initialise the target directory
     */
    void setTargetDirectory(String directory);

    /**
     * @return Get the target package for the current configuration
     */
    String getTargetPackage();

    /**
     * Initialise the target package name
     */
    void setTargetPackage(String packageName);

    /**
     * @return Get the target locale for the current configuration
     */
    Locale getTargetLocale();

    /**
     * Initialise the target locale
     */
    void setTargetLocale(Locale targetLocale);

    /**
     * @return Get the target language for the current configuration
     */
    Language getTargetLanguage();

    /**
     * Initialise the target language
     */
    void setTargetLanguage(Language targetLanguage);

    /**
     * Whether fields are instance fields (as opposed to static fields)
     */
    void setInstanceFields(boolean instanceFields);

    /**
     * Whether fields are instance fields (as opposed to static fields)
     */
    boolean getInstanceFields();

    /**
     * Whether getters and setters should be generated JavaBeans style (or jOOQ
     * style).
     */
    void setJavaBeansGettersAndSetters(boolean javaBeansGettersAndSetters);

    /**
     * Whether getters and setters should be generated JavaBeans style (or jOOQ
     * style).
     */
    boolean getJavaBeansGettersAndSetters();

    /**
     * Whether names of unambiguous {@link ForeignKeyDefinition} should be based
     * on the referenced {@link TableDefinition}.
     * <p>
     * When a child table has only one {@link ForeignKeyDefinition} towards a
     * parent table, then that path is "unambiguous." In that case, some
     * {@link GeneratorStrategy} implementations may choose to use the parent
     * table's {@link TableDefinition} for implementations of
     * {@link #getJavaMethodName(Definition)}, instead of the
     * {@link ForeignKeyDefinition}, e.g. for implicit join paths.
     * <p>
     * This flag allows for turning off this default behaviour.
     */
    void setUseTableNameForUnambiguousFKs(boolean useTableNameForUnambiguousFKs);

    /**
     * Whether names of unambiguous {@link ForeignKeyDefinition} should be based
     * on the referenced {@link TableDefinition}.
     * <p>
     * When a child table has only one {@link ForeignKeyDefinition} towards a
     * parent table, then that path is "unambiguous." In that case, some
     * {@link GeneratorStrategy} implementations may choose to use the parent
     * table's {@link TableDefinition} for implementations of
     * {@link #getJavaMethodName(Definition)}, instead of the
     * {@link ForeignKeyDefinition}, e.g. for implicit join paths.
     * <p>
     * This flag allows for turning off this default behaviour.
     */
    boolean getUseTableNameForUnambiguousFKs();

    // -------------------------------------------------------------------------
    // XXX: The SPI
    // -------------------------------------------------------------------------

    /**
     * This is applied to enum literals of a given {@link EnumDefinition}.
     *
     * @return The Java identifier representing this enum literal, e.g. [OK]
     */
    String getJavaEnumLiteral(EnumDefinition definition, String literal);

    /**
     * @see #getJavaEnumLiteral(EnumDefinition, String)
     */
    List<String> getJavaEnumLiterals(EnumDefinition definition, Collection<? extends String> literals);

    /**
     * @see #getJavaEnumLiteral(EnumDefinition, String)
     */
    List<String> getJavaEnumLiterals(EnumDefinition definition, String... literals);

    /**
     * This is applied to definitions that can result in reference static and
     * instance members. For instance, the reference instance of a
     * {@link TableDefinition} is a java identifier
     *
     * @return The Java identifier representing this object, e.g. [my_table]
     */
    String getJavaIdentifier(Definition definition);

    /**
     * @see #getJavaIdentifier(Definition)
     */
    List<String> getJavaIdentifiers(Collection<? extends Definition> definitions);

    /**
     * @see #getJavaIdentifier(Definition)
     */
    List<String> getJavaIdentifiers(Definition... definitions);

    /**
     * This is applied to definitions that can result in reference static and
     * instance members. For instance, the reference instance of a
     * {@link TableDefinition} is a java identifier
     *
     * @return The Java identifier representing this object, e.g. [my_table]
     */
    String getFullJavaIdentifier(Definition definition);

    /**
     * @see #getFullJavaIdentifier(Definition)
     */
    List<String> getFullJavaIdentifiers(Collection<? extends Definition> definitions);

    /**
     * @see #getFullJavaIdentifier(Definition)
     */
    List<String> getFullJavaIdentifiers(Definition... definitions);

    /**
     * This is applied to definitions that can result in setters of a container.
     * For example, the definition could be a {@link ColumnDefinition}, the
     * container a {@link TableDefinition}. Then this would apply to records and
     * POJOs. Also, the definition could be an {@link AttributeDefinition} and
     * the container a {@link UDTDefinition}
     * <p>
     * This is the same as calling
     * <code>getJavaSetterName(definition, Mode.DEFAULT)</code>
     *
     * @return The Java setter method name representing this object, e.g.
     *         [setMyTable]
     */
    String getJavaSetterName(Definition definition);

    /**
     * This is applied to definitions that can result in setters of a container.
     * For example, the definition could be a {@link ColumnDefinition}, the
     * container a {@link TableDefinition}. Then this would apply to records and
     * POJOs. Also, the definition could be an {@link AttributeDefinition} and
     * the container a {@link UDTDefinition}
     *
     * @return The Java setter method name representing this object, e.g.
     *         [setMyTable]
     */
    String getJavaSetterName(Definition definition, Mode mode);

    /**
     * This is applied to definitions that can result in getters of a container.
     * For example, the definition could be a {@link ColumnDefinition}, the
     * container a {@link TableDefinition}. Then this would apply to records and
     * POJOs. Also, the definition could be an {@link AttributeDefinition} and
     * the container a {@link UDTDefinition}
     * <p>
     * This is the same as calling
     * <code>getJavaGetterName(definition, Mode.DEFAULT)</code>
     *
     * @return The Java getter method name representing this object, e.g.
     *         [getMyTable]
     */
    String getJavaGetterName(Definition definition);

    /**
     * This is applied to definitions that can result in getters of a container.
     * For example, the definition could be a {@link ColumnDefinition}, the
     * container a {@link TableDefinition}. Then this would apply to records and
     * POJOs. Also, the definition could be an {@link AttributeDefinition} and
     * the container a {@link UDTDefinition}
     *
     * @return The Java getter method name representing this object, e.g.
     *         [getMyTable]
     */
    String getJavaGetterName(Definition definition, Mode mode);

    /**
     * This is applied to definitions that can result in methods. For example,
     * the definition could be a {@link RoutineDefinition}
     * <p>
     * This is the same as calling
     * <code>getJavaMethodName(definition, Mode.DEFAULT)</code>
     *
     * @return The Java method name representing this object, e.g. [myFunction]
     */
    String getJavaMethodName(Definition definition);

    /**
     * This is applied to definitions that can result in methods. For example,
     * the definition could be a {@link RoutineDefinition}
     *
     * @return The Java method name representing this object, e.g. [myFunction]
     */
    String getJavaMethodName(Definition definition, Mode mode);

    /**
     * @return The super class name of the global references class for a given
     *         definition type, e.g. [com.example.AbstractPojo]. If this returns
     *         <code>null</code> or an empty string, then no super class is
     *         extended.
     */
    String getGlobalReferencesJavaClassExtends(Definition container, Class<? extends Definition> objectType);

    /**
     * This is the same as calling
     * <code>getJavaClassExtends(definition, Mode.DEFAULT)</code>
     *
     * @return The super class name of the Java class representing this object,
     *         e.g. [com.example.AbstractPojo]. If this returns
     *         <code>null</code> or an empty string, then no super class is
     *         extended.
     */
    String getJavaClassExtends(Definition definition);

    /**
     * @return The super class name of the Java class representing this object,
     *         e.g. [com.example.AbstractPojo]. If this returns
     *         <code>null</code> or an empty string, then no super class is
     *         extended.
     */
    String getJavaClassExtends(Definition definition, Mode mode);

    /**
     * @return The implemented interface names of the global references class
     *         for a given definition type, e.g. [com.example.Pojo]. If this
     *         returns <code>null</code> or an empty list, then no interfaces
     *         are implemented.
     */
    List<String> getGlobalReferencesJavaClassImplements(Definition container, Class<? extends Definition> objectType);

    /**
     * This is the same as calling
     * <code>getJavaClassImplements(definition, Mode.DEFAULT)</code>
     *
     * @return The implemented interface names of the Java class name
     *         representing this object, e.g. [com.example.Pojo] If this returns
     *         <code>null</code> or an empty list, then no interfaces are
     *         implemented.
     */
    List<String> getJavaClassImplements(Definition definition);

    /**
     * @return The implemented interface names of the Java class name
     *         representing this object, e.g. [com.example.Pojo]. If this
     *         returns <code>null</code> or an empty list, then no interfaces
     *         are implemented.
     */
    List<String> getJavaClassImplements(Definition definition, Mode mode);

    /**
     * @return The Java class name of the global references class for a given
     *         definition type, e.g. [MyTableSuffix]
     */
    String getGlobalReferencesJavaClassName(Definition container, Class<? extends Definition> objectType);

    /**
     * This is the same as calling
     * <code>getJavaClassName(definition, Mode.DEFAULT)</code>
     *
     * @return The Java class name representing this object, e.g. [MyTable]
     */
    String getJavaClassName(Definition definition);

    /**
     * @return The Java class name representing this object, e.g.
     *         [MyTableSuffix]
     */
    String getJavaClassName(Definition definition, Mode mode);

    /**
     * @return The Java package name of the global references class for a given
     *         definition type, e.g. [org.jooq.generated]
     */
    String getGlobalReferencesJavaPackageName(Definition container, Class<? extends Definition> objectType);

    /**
     * This is the same as calling
     * <code>getJavaPackageName(definition, Mode.DEFAULT)</code>
     *
     * @return The Java package name of this object, e.g. [org.jooq.generated]
     */
    String getJavaPackageName(Definition definition);

    /**
     * @return The Java package name of this object, e.g. [org.jooq.generated]
     */
    String getJavaPackageName(Definition definition, Mode mode);

    /**
     * The "java member name" is applied where a definition is used as a member
     * (for POJOs) or as a method argument (for setters). Example definitions
     * are
     * <ul>
     * <li>{@link ColumnDefinition}</li>
     * <li>{@link ParameterDefinition}</li>
     * <li>{@link AttributeDefinition}</li>
     * </ul>
     * This is the same as calling
     * <code>getJavaMemberName(definition, Mode.DEFAULT)</code>
     *
     * @return The Java class name representing this object, starting with a
     *         lower case character, e.g. [myTable]
     */
    String getJavaMemberName(Definition definition);

    /**
     * The "java member name" is applied where a definition is used as a member
     * (for POJOs) or as a method argument (for setters). Example definitions
     * are
     * <ul>
     * <li>{@link ColumnDefinition}</li>
     * <li>{@link ParameterDefinition}</li>
     * <li>{@link AttributeDefinition}</li>
     * </ul>
     *
     * @return The Java class name representing this object, starting with a
     *         lower case character, e.g. [myTableSuffix]
     */
    String getJavaMemberName(Definition definition, Mode mode);

    /**
     * @return The full Java class name of the global references class for a
     *         given definition type, e.g. [org.jooq.generated.MyTable]
     */
    String getGlobalReferencesFullJavaClassName(Definition container, Class<? extends Definition> objectType);

    /**
     * @return The full Java class name representing this object, e.g.
     *         [org.jooq.generated.MyTable]
     */
    String getFullJavaClassName(Definition definition);

    /**
     * This is the same as calling
     * <code>getFullJavaClassName(definition, Mode.DEFAULT)</code>
     *
     * @return The full Java class name representing this object, e.g.
     *         [org.jooq.generated.MyTable][suffix]
     */
    String getFullJavaClassName(Definition definition, Mode mode);

    /**
     * @return The Java class file name of the global references class for a
     *         given definition type, e.g. [MyTable.java]
     */
    String getGlobalReferencesFileName(Definition container, Class<? extends Definition> objectType);

    /**
     * @return The Java class file name representing this object, e.g.
     *         [MyTable.java]
     */
    String getFileName(Definition definition);

    /**
     * @return The Java class file name representing this object, e.g.
     *         [MyTableSuffix.java]
     */
    String getFileName(Definition definition, Mode mode);

    /**
     * @return The directory containing all Java objects, e.g.
     *         [C:\org\jooq\generated]
     */
    File getFileRoot();

    /**
     * @return The Java class file name of the global references class for a
     *         given definition type, e.g. [C:\org\jooq\generated\MyTable.java]
     */
    File getGlobalReferencesFile(Definition container, Class<? extends Definition> objectType);

    /**
     * @return The Java class file name representing this object, e.g.
     *         [C:\org\jooq\generated\MyTable.java]
     */
    File getFile(Definition definition);

    /**
     * @return The Java class file name representing this object, e.g.
     *         [C:\org\jooq\generated\MyTableSuffix.java]
     */
    File getFile(Definition definition, Mode mode);

    /**
     * @return The Java class file name representing this object, e.g.
     *         [C:\org\jooq\generated\fileName]
     */
    File getFile(String fileName);

    /**
     * @return The Java class file header of the global references class for a
     *         given definition type, e.g. <pre><code>
     * This file is generated by jOOQ.
     * </code></pre>
     */
    String getGlobalReferencesFileHeader(Definition container, Class<? extends Definition> objectType);

    /**
     * @return The Java class file header, e.g. <pre><code>
     * This file is generated by jOOQ.
     * </code></pre>
     */
    String getFileHeader(Definition definition);

    /**
     * @return The Java class file header, e.g. <pre><code>
     * This file is generated by jOOQ.
     * </code></pre>
     */
    String getFileHeader(Definition definition, Mode mode);

    /**
     * @return The overload suffix to be applied when generating overloaded
     *         routine artefacts, e.g.
     *         <code>"_OverloadIndex_" + overloadIndex</code>
     */
    String getOverloadSuffix(Definition definition, Mode mode, String overloadIndex);

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
