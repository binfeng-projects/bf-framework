package ${corePackage}.test;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.test.base.BaseCoreTest;
import org.bf.framework.test.codegen.CodeGenTool;
import org.bf.framework.test.codegen.GenConfig;
import org.junit.jupiter.api.Test;

@Slf4j
public class SpringTest implements BaseCoreTest {
    // 必须在spring环境中执行

    /**
     * 生成中间件框架默认装配好的所有bean的name。供使用
     * 顺便可以测试集成是否有问题
     * 如果能正常生成，说明你集成的中间件都没有问题。
     */
    @Test
    public void geneMiddleWareHolder() throws Exception{
        GenConfig cfg = new GenConfig().setPackageCore("${corePackage}");
        new CodeGenTool(cfg).geneMiddleWareHolder();
    }
}
