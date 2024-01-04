[#include "common.ftl"]
/**
 * ${table.comment}
 */
public interface ${clientRpc} {

    Result<${clientDto}>get(${clientDto} dto);

    PageResult<${clientDto}>list(PageResult<${clientDto}>dto);
    //    Result<Boolean> save(${clientDto} dto);
    //    Result<Boolean> edit(${clientDto} dto);
}

