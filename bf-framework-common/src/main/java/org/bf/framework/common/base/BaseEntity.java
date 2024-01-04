package org.bf.framework.common.base;

import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@FieldNameConstants
public abstract class BaseEntity<PK extends Number> extends BaseField<PK> {
    public BaseEntity() {
    }

    public abstract String getSchemaName();
    public abstract String getTableName();

    /**
     * 创建时间
     */
    protected LocalDateTime createdAt;
    /**
     * 更新时间
     */
    protected LocalDateTime updatedAt;
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
