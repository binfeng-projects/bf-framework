package org.bf.framework.autoconfigure.jooq;

import org.bf.framework.common.base.BaseDao;
import org.bf.framework.common.base.BaseEntity;
import org.bf.framework.common.result.PageResult;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;
import org.jooq.*;
import org.jooq.impl.DAOImpl;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.*;
import java.util.Collection;
import java.util.List;

public abstract class BaseDaoImpl<R extends UpdatableRecord<R>,E extends BaseEntity<PK>,PK extends Number> extends DAOImpl<R, E, PK> implements BaseDao<PK,E> {
    protected BaseDaoImpl(Table<R> table, Class<E> type) {
        super(table,type);
    }
    @Override
    public void insertSelective(E e) {
        super.insert(e);
    }

    @Override
    public void insertBatch(Collection<E> list) {
        super.insert(list);
    }

    @Override
    public Long countByWhere(E e) {
        return ctx()
                .selectCount()
                .from(getTable())
                .where(whereCondition(e))
                .fetchOne(0, Long.class);
    }

    @Override
    public E getOneByWhere(E e) {
        return ctx()
                .selectFrom(getTable())
                .where(whereCondition(e))
                .fetchOne(mapper());
    }
    @Override
    public List<E> listByWhere(PageResult<E> p) {
        return ctx()
                .selectFrom(getTable())
                .where(whereCondition(p.getBody()))
                .limit(p.getSize())
                .offset(p.getOffset())
                .fetch(mapper());
    }

    @Override
    public Long maxIdByWhere(E e) {
        Field<?> maxIdField = max(pk());
        return ctx()
                .select(maxIdField)
                .from(getTable())
                .where(whereCondition(e))
                .fetchOne(0, Long.class);
    }
    @Override
    public void deleteByWhere(E e) {
        ctx()
                .deleteFrom(getTable())
                .where(whereCondition(e))
                .execute();
    }

    @Override
    public void deleteById(PK id) {
        super.deleteById(id);
    }

    @Override
    public void deleteByIds(Collection<PK> ids) {
        super.deleteById(ids);
    }
    @Override
    public List<E> listByIds(Collection<PK> ids) {
        Field<PK> idField = pk();
        if(CollectionUtils.isEmpty(ids)) {
            return null;
        }
        return ctx()
                .selectFrom(getTable())
                .where(idField.in(ids))
                .fetch(mapper());
    }
    @Override
    public E getById(PK id) {
        return super.findById(id);
    }

    @Override
    public void updateSelective(E e) {
        super.update(e);
    }

    @Override
    public List<E> selectStartEnd(Number startId, Number endId) {
        Field<PK> idField = pk();
        Condition condition = idField.ge((PK)startId).and(idField.lt((PK)endId));
        return ctx()
                .selectFrom(getTable())
                .where(condition)
                .fetch(mapper());
    }

    @Override
    public List<E> selectStartLimit(Number startId, Long limit) {
        Field<PK> idField = pk();
        return ctx()
                .selectFrom(getTable())
                .where(idField.ge((PK)startId))
                .orderBy(idField)
                .limit(PAGE_SIZE)
                .fetch(mapper());
    }

    @Override
    public Long maxId() {
        Field<?> maxIdField = max(pk());
        return ctx()
                .select(maxIdField)
                .from(getTable())
                .fetchOne(0, Long.class);
    }

    public PK getId(E e) {
        return e.getId();
    }

    private /* non-final */ TableField<R,PK> pk() {
        UniqueKey<R> key = getTable().getPrimaryKey();
        return key == null ? null : (TableField<R,PK>)key.getFieldsArray()[0];
    }

    private Condition whereCondition (E e) {
        Condition dynamic = dynamicCondition(e);
        Condition cd = dynamic == null ? DSL.trueCondition() : dynamic;
        Field<PK> idField = pk();
        if (getId(e) != null) { //id = xxx
            cd.and(idField.eq(getId(e)));
        }
        if(CollectionUtils.isNotEmpty(e.getIds())) {
            Field<?> inField;
            if(StringUtils.isNotBlank(e.getQueryInFieldName())) {
                inField = field(e.getQueryInFieldName());
            } else {
                inField = idField;
            }
            cd.and(inField.in(e.getIds()));
        }
        return cd;
    }
    public abstract Condition dynamicCondition(E e);
}
