[#include "common.ftl"]
/**
 * ${table.comment}
 */
@Slf4j
@DubboService
public class ${serverRpc} implements ${clientRpc} {
    @Autowired
    ${daoProxy} proxy;

    public Result<${clientDto}>get(${clientDto} dto) {
        return Result.of(proxy.get(dto));
    }

    public PageResult<${clientDto}>list(PageResult<${clientDto}>dto) {
        return proxy.listPage(dto);
    }
//    public Result<Boolean> save(${clientDto} dto){
//        proxy.save(dto);
//        return Result.of(true);
//    }
//
//    public Result<Boolean> edit(${clientDto} dto) {
//        proxy.edit(dto);
//        return Result.of(true);
//    }
}

