package org.bf.framework.common.base;

import java.io.Serializable;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.experimental.FieldNameConstants;
import org.bf.framework.common.util.valid.Edit;
import org.bf.framework.common.util.valid.EditPart;

@FieldNameConstants
public abstract class BaseField<PK extends Number> implements PkAble<PK> {
    public BaseField() {
    }

    /**
     * 主键id
     */
    @NotNull(groups = {Edit.class, EditPart.class})
    protected PK id;

    /**
     * 版本，乐观锁字段
     */
    protected Long version;
    /**
     * 是否已删除, 0:未删除, 1
     */
//    protected boolean deleted;

    /**
     * 根据某个字段in查询
     * 如果不传，默认id 作in查询
     */
    protected String queryInFieldName;

    /**
     * 根据某个字段in查询
     */
    @Size(max = 500)
    protected Collection<? extends Serializable> ids;

    @Override
    public PK getId() {
        return id;
    }

    public void setId(PK id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getQueryInFieldName() {
        return queryInFieldName;
    }

    public void setQueryInFieldName(String queryInFieldName) {
        this.queryInFieldName = queryInFieldName;
    }

    public Collection<? extends Serializable> getIds() {
        return ids;
    }

    public void setIds(Collection<? extends Serializable> ids) {
        this.ids = ids;
    }

}
