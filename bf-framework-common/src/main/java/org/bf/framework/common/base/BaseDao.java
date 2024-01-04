package org.bf.framework.common.base;


import org.bf.framework.common.result.PageResult;

import java.util.Collection;
import java.util.List;

public interface BaseDao<PK extends Number,E extends BaseEntity<PK>> extends StartEndSelect<PK,E>{

    /**
     * 动态拼接insert语句，如果对象的属性不为空，会被插入，如果为空，会被忽略
     * @param e
     */
    void insertSelective(E e);

    /**
     * 批量插入
     * @param list
     * @return
     */
    void insertBatch(Collection<E> list);

    /**
     * 根据条件返回查询总数
     * @param e 按需拼接where字段
     * @return
     */
    Long countByWhere(E e);

    /**
     * 根据条件删除，高危接口，谨慎使用
     * @param e 按需拼接where字段
     * @return
     */
    void deleteByWhere(E e);
    /**
     * 返回单个对象
     * @param e 按需拼接where条件
     * @return
     */
    E getOneByWhere(E e);
    void deleteById(PK id);
    /**
     * 返回单个对象
     * @param id 根据id查
     * @return
     */
    E getById(PK id);
    /**
     * 本着最小sql的原则而提供,支持单个ID作条件更新，批量ID作条件更新
     * @param e 按需拼接update字段
     * @return
     */
    void updateSelective(E e);

    /**
     * 返回单个列表
     * @param p 按需拼接where条件,并限制分页
     * @return
     */
    List<E> listByWhere(PageResult<E> p);

    void deleteByIds(Collection<PK> ids);
    List<E> listByIds(Collection<PK> ids);
    Long maxIdByWhere(E e);
//    Integer logicRemoveByWhere(T po);
}