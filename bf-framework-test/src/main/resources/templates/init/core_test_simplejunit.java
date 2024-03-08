package ${corePackage};

import org.junit.jupiter.api.Test;

import org.bf.framework.test.codegen.CodeGenTool;
/**
 * 有时候也需要一些
 * 脱离spring环境的单元测试
 */
public class SimpleJunitTest {

    @Test
    public void testSomething(){

    }
    //代码脚手架，生成一个空工程，类似SpringBootInitializer
    @Test
    public void initProject(){
        CodeGenTool.initProject("/Users/bf/tech/workspace","${appName}","${corePackage}",null,null);
//        CodeGenTool.initProject("/Users/bf/tech/workspace","bf-pay","com.bf.pay.core","com.bf.middleware","binfeng-middleware");
    }
}
