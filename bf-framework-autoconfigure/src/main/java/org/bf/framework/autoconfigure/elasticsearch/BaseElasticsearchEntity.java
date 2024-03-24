package org.bf.framework.autoconfigure.elasticsearch;

import lombok.experimental.FieldNameConstants;
import org.bf.framework.common.base.PkAble;
import org.bf.framework.common.util.valid.Edit;
import org.bf.framework.common.util.valid.EditPart;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@FieldNameConstants
public abstract class BaseElasticsearchEntity<PK extends Serializable> implements PkAble<PK> {
    public BaseElasticsearchEntity() {
    }
    /**
     * 主键id
     */
    @NotNull(groups = {Edit.class, EditPart.class})
    @Id
    protected PK id;
    /**
     * 创建时间
     */
    @CreatedDate
    @Field(name = "created_at",type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Field(name = "updated_at",type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime updatedAt;
    /**
     * 版本，乐观锁字段
     */
    @Version
    @Field(type = FieldType.Long)
    protected Long version;
    /**
     * 是否已删除, 0:未删除, 1
     */

//    /**
//     * 图链接
//     */
//    @Field(type = FieldType.Keyword, index = false)
//    String coverImage;
//
//    /**
//     * 标题
//     */
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", copyTo = "descriptiveContent")
//    String title;
//
//    /**
//     * 描述
//     */
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", copyTo = "descriptiveContent")
//    String description;
    /**
     * 由其他属性copy而来，主要用于搜索功能，不需要储存数据
     */
//    @JsonIgnore
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", ignoreFields = "descriptiveContent", excludeFromSource = true)
//    private String descriptiveContent;
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
