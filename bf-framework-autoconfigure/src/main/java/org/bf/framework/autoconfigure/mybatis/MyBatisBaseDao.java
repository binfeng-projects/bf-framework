package org.bf.framework.autoconfigure.mybatis;

import org.apache.ibatis.annotations.Param;
import org.bf.framework.common.base.BaseDao;
import org.bf.framework.common.base.BaseEntity;

import java.util.List;

public interface MyBatisBaseDao <PK extends Number,E extends BaseEntity<PK>>extends BaseDao<PK,E> {
    @Override
    List<E> selectStartEnd(@Param("startId") Number startId,@Param("endId") Number endId);
    @Override
    List<E> selectStartLimit(@Param("startId") Number startId, @Param("size") Long size);
    @Override
    List<E> listByWhere(@Param("p")E p,@Param("size") Number size,@Param("offset")Number offset);
}
