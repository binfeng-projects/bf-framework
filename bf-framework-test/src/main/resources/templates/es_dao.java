[#include "common.ftl"]
package ${currentPackage};
import org.bf.framework.autoconfigure.elasticsearch.BaseElasticsearchRepository;
[#list importClass as cls]
import ${cls};
[/#list]
/**
 * ${table.comment}
 */
public interface ${className}${classSuffix} extends BaseElasticsearchRepository<${entity},${pkType}> {

}
