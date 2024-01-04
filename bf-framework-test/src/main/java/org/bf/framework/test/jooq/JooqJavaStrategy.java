package org.bf.framework.test.jooq;

import lombok.extern.slf4j.Slf4j;
import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.TableDefinition;

@Slf4j
public class JooqJavaStrategy extends DefaultGeneratorStrategy {

    @Override
    public String getJavaPackageName(Definition definition, Mode mode) {
        String str = super.getJavaPackageName(definition, mode);
        if (definition instanceof TableDefinition) {
            if(!mode.equals(Mode.DEFAULT)) {
                str = str.replace(".tables","");
            }
            //pojo和interface放在外层，也可以作为mybatis的模型
            if(mode.equals(Mode.INTERFACE) || mode.equals(Mode.POJO)) {
                str = str.replace(".jooq",".db");
            }
            if(mode.name().startsWith("CLIENT_")) {
                str = str.replace(".core",".client");
                if(mode.equals(Mode.CLIENT_DTO)) {
                    str = str.replace(".jooq","") + ".dto";
                }
                if(mode.equals(Mode.CLIENT_RPC)) {
                    str = str.replace(".jooq","") + ".rpc";
                }
            } else if(mode.name().startsWith("SERVER_")) {
                str = str.replace(".core",".server");
                if(mode.equals(Mode.SERVER_CTL)) {
                    str = str.replace(".jooq","")  +".ctl";
                }
                if(mode.equals(Mode.SERVER_RPC)) {
                    str = str.replace(".jooq","") + ".rpc";
                }
                if(mode.equals(Mode.SERVER_TEST)) {
                    str = str.replace(".jooq",".test.ctl") ;
                }
            } else {
                if(mode.equals(Mode.CORE_TEST)) {
                    str = str.replace(".jooq",".test.proxy");
                }
                if(mode.equals(Mode.CORE_INTERFACE)) {
                    str = str.replace(".jooq",".db");
                }
                if(mode.equals(Mode.CORE_CONVERT)) {
                    str = str.replace(".jooq",".convert");
                }
                if(mode.equals(Mode.CORE_DAOPROXY)) {
                    //proxy代码是需要改动的，生成到主目录里
                    str = str.replace(".jooq",".proxy");
                }
            }
            if(mode.equals(Mode.INTERFACE)) { //pojo和interface放在外层，也可以作为mybatis的模型
                return str.replace(".interfaces",".ipojo");
            }
            if(mode.equals(Mode.POJO)) { //pojo和interface放在外层，也可以作为mybatis的模型
                return str.replace(".pojos",".entity");
            }
        }
        return str;
    }

    public String getJavaClassName(Definition definition, Mode mode) {
        String str = super.getJavaClassName(definition, mode);
        if (definition instanceof TableDefinition) {
            if(mode.equals(Mode.POJO)) {
                return str + "Entity";
            }
            if(mode.equals(Mode.DAO)) {
                return str + "Impl";
            }
            if(mode.name().startsWith("CLIENT_")) {
                if(mode.equals(Mode.CLIENT_DTO)) {
                    return str + "DTO";
                }
                if(mode.equals(Mode.CLIENT_RPC)) {
                    return str + "Api";
                }
            } else if(mode.name().startsWith("SERVER_")) {
                if(mode.equals(Mode.SERVER_CTL)) {
                    return str + "Ctl";
                }
                if(mode.equals(Mode.SERVER_RPC)) {
                    return str + "ApiImpl";
                }
                if(mode.equals(Mode.SERVER_TEST)) {
                    return "Test" + str + "Ctl";
                }
            } else {
                if(mode.equals(Mode.CORE_TEST)) {
                    return "Test" + str + "Proxy";
                }
                if(mode.equals(Mode.CORE_INTERFACE)) {
                    return str + "Dao";
                }
                if(mode.equals(Mode.CORE_CONVERT)) {
                    return str + "Convert";
                }
                if(mode.equals(Mode.CORE_DAOPROXY)) {
                    return str + "Proxy";
                }
            }
        }
        if (definition instanceof SchemaDefinition) {
            if(mode.equals(Mode.CORE_JOOQBASEDAO)) {
                return str + "BaseDaoImpl";
            }
        }
        return str;
    }
    @Override
    public String getFileHeader(Definition definition, Mode mode) {
        return "This file is auto generated";
    }
}
