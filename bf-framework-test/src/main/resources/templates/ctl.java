[#include "common.ftl"]

import static ${serverCtlFull}.RESOURCE;
/**
 * ${table.comment}
 */
@Slf4j
@RestController
@RequestMapping(RESOURCE)
public class ${serverCtl} {

    public static final String RESOURCE = "${table.sqlName}";

    @Autowired
    ${daoProxy} proxy;

    @PostMapping("/get")
    @PermCheck(resource = RESOURCE)
    public Result<?> get(@RequestBody ${clientDto} dto) {
        return Result.of(proxy.get(dto));
    }

    @PostMapping("/list")
    @PermCheck(resource = RESOURCE)
    public PageResult<?> list(@RequestBody PageResult<${clientDto}> dto) {
        return proxy.listPage(dto);
    }

    @PostMapping("/save")
    @PermCheck(resource = RESOURCE,perm = 1)
    public Result<Boolean> save(@RequestBody ${clientDto} dto){
        proxy.save(dto);
        return Result.of(true);
    }

    @PostMapping("/edit")
    @PermCheck(resource = RESOURCE,perm = 2)
    public Result<Boolean> edit(@RequestBody ${clientDto} dto) {
        proxy.edit(dto);
        return Result.of(true);
    }


    @PostMapping("/removeById")
    @PermCheck(resource = RESOURCE,perm = 4)
    public Result<Boolean> removeById(${pkType} id){
        proxy.removeById(id);
        return Result.of(true);
    }

    @PostMapping("/removeByIds")
    @PermCheck(resource = RESOURCE,perm = 4)
    public Result<Boolean> removeByIds(@RequestBody List<${pkType}> ids){
        proxy.removeByIds(ids);
        return Result.of(Boolean.TRUE);
    }
}

