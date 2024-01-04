package org.bf.framework.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.List;

/**
 * 支持分页查询
 * @param <T>
 */
@FieldNameConstants
public class PageResult<T> extends Result<T>{
    private static final long defaultSize = 50L;
    private static int defaultMaxPage = 50;
    private static int defaultMaxSize = 1000;

    private Long total;
    private Long size;
    private Long page;
    private List<T> records;
    /**
     * 限制最大允许的页数和最多返回的条数,两个参数均可以为空
     * 任何查询接口理论上都应该有分页保护，否则会有很多隐患，比如拖库，或者返回太多，影响性能
     */
    public long getTotalPage() {
        long result = 0;
        if (this.total != null && this.size != null) {
            result = this.total / this.size;
            if (this.total % this.size != 0) {
                ++result;
            }
        }
        if (result == 0) {
            result = 1;
        }
        return result;
    }
    public long getOffset() {
        return (page - 1) * size;
    }
    public long getLimit() {
        return size;
    }

    public void setSize(Long size) {
        if (size == null || size <= 0 || size > defaultMaxSize) {
            this.size = defaultSize;
        } else {
            this.size = size;
        }
    }
    public void setPage(Long page) {
        if (page == null || page <= 0 || page > defaultMaxPage) {
            this.page = 1L;
        } else {
            this.page = page;
        }
    }
    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    }
    public List<T> getRecords() {
        return records;
    }
    public void setRecords(List<T> records) {
        this.records = records;
    }
    public Long getSize() {
        return size;
    }

    public Long getPage() {
        return page;
    }

}
