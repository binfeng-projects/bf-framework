package org.bf.framework.common.base;


import lombok.experimental.FieldNameConstants;

import jakarta.validation.constraints.Size;
import org.bf.framework.common.util.valid.Normal;
import org.bf.framework.common.util.valid.ValidUtil;

@FieldNameConstants
public abstract class BaseDTO<PK extends Number> extends BaseField<PK> implements ValidUtil{
    public BaseDTO() {
    }

    /**
     * 创建时间
     */
    @Size(min = 9,max = 30,groups = {Normal.class})
    protected String createdAt;
    /**
     * 更新时间
     */
    @Size(min = 9,max = 30,groups = {Normal.class})
    protected String updatedAt;
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
