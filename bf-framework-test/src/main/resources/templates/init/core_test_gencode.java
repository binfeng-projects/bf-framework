package ${corePackage}.test;

import lombok.extern.slf4j.Slf4j;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.test.base.BaseCoreTest;
import org.bf.framework.test.codegen.CodeGenTool;
import org.bf.framework.test.codegen.GenConfig;
import org.junit.jupiter.api.Test;

import static org.bf.framework.boot.constant.MiddlewareConst.PREFIX_DATASOURCE;

@Slf4j
public class TestGenCode implements BaseCoreTest {
    // 必须在spring环境中执行
    @Test
    public void testGenCode() throws Exception{
        GenConfig cfg = new GenConfig().setPackageCore("${corePackage}");
        cfg.setMiddlewarePrefix(CollectionUtils.newArrayList(PREFIX_DATASOURCE));
        new CodeGenTool(cfg).geneCode();
    }
}
