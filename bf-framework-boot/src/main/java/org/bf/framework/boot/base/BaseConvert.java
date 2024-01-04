package org.bf.framework.boot.base;

import org.bf.framework.common.base.BaseDTO;
import org.bf.framework.common.base.BaseEntity;
import org.bf.framework.common.constant.Const;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * 可能lombok和mapstruct会有先后冲突
 * 用lombok-mapstruct-binding并没有奏效
 * 解决方案是maven分module依赖，让底层的module先引入lombok，
 * 让上层的module引入mapstruct
 */
@MapperConfig
public interface BaseConvert<PK extends Number,DTO extends BaseDTO<PK>,Entity extends BaseEntity<PK>> {

    /**
     * 映射同名属性
     */
    @Mapping(target = BaseDTO.Fields.createdAt, dateFormat = Const.DATA_YMDHMS)
    @Mapping(target = BaseDTO.Fields.updatedAt, dateFormat = Const.DATA_YMDHMS)
    DTO entityToDto(Entity p);

    /**
     * 映射同名属性，集合形式
     */
    @InheritConfiguration(name = Const.CONVERT_ENTITY_DTO)
    List<DTO> entityToDtoList(Collection<Entity> entity);

    /**
     * 反向，映射同名属性
     */
    @InheritInverseConfiguration(name = Const.CONVERT_ENTITY_DTO)
    Entity dtoToEntity(DTO dto);


    /**
     * 反向，映射同名属性，集合形式
     */
    @InheritConfiguration(name = Const.CONVERT_DTO_ENTITY)
    List<Entity> dtoToEntityList(Collection<DTO> var);

    /**
     * 映射同名属性，集合流形式
     */
    List<DTO> entityToDto(Stream<Entity> stream);

    /**
     * 反向，映射同名属性，集合流形式
     */
    List<Entity> dtoToEntity(Stream<DTO> stream);
}