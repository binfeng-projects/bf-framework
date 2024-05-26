package org.bf.framework.boot.base;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.bf.framework.boot.support.mq.MqProducer;
import org.bf.framework.boot.util.SpringUtil;
import org.bf.framework.common.base.BaseDTO;
import org.bf.framework.common.base.BaseDao;
import org.bf.framework.common.base.BaseEntity;
import org.bf.framework.common.base.StartEndSelect;
import org.bf.framework.common.constant.Const;
import org.bf.framework.common.result.PageResult;
import org.bf.framework.common.util.MapUtils;
import org.bf.framework.common.util.StringUtils;
import org.bf.framework.common.util.sql.CanalMessage;
import org.bf.framework.common.util.valid.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * javax.validation.ConstraintDeclarationException: HV000151: A method overriding another method must not
 * redefine the parameter constraint configuration
 * hibernate-validator规则，这个规则限制了数据校验约束不能应用在重写方法的参数上，换句话说如果接口没有定义约束，那么实现类就不能够定义约束
 *
 * AOP 的 校验和自身的校验相结合。AOP的方法校验不设置任何group，只校验入口对象的null与否
 * 具体业务逻辑自己根据分组校验字段信息，两者结合，代码最简洁
 */
@Slf4j
@Validated
public class BaseDaoProxy<PK extends Number,DTO extends BaseDTO<PK>,Entity extends BaseEntity<PK>> implements Const, StartEndSelect<PK,DTO> {
    protected Class<DTO> dtoClass;
    protected Class<Entity> entityClass;
    public BaseDaoProxy() {
        Class<?> c = getClass();
        Type t = c.getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            Type[] p = ((ParameterizedType) t).getActualTypeArguments();
            this.dtoClass = (Class<DTO>) p[1];
            this.entityClass = (Class<Entity>) p[2];
        }
    }
    @Autowired
    protected BaseDao<PK,Entity> dao;
    @Autowired
    protected BaseConvert<PK,DTO,Entity> convert;
    @Override
    public List<DTO> selectStartEnd(Number startId, Number endId) {
        return convert.entityToDtoList(dao.selectStartEnd(startId,endId));
    }

    @Override
    public List<DTO> selectStartLimit(Number startId, Long limit) {
        return convert.entityToDtoList(dao.selectStartLimit(startId,limit));
    }
    public <T> void doEtl(MqProducer<T> mq,final String topic) {
        dao.doScan(0L,null,true,e -> {
            CanalMessage msg = new CanalMessage();
            String schema = e.getSchemaName();
            String table = e.getTableName();
            msg.setDatabase(schema);
            msg.setTable(table);
            msg.setIsDdl(false);
            msg.setType("UPDATE");
            msg.setTs(System.currentTimeMillis());
//            msg.setMysqlType(mysqlType);
//            msg.setSqlType(sqlType);
            List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
            msg.setData(dataList);
            dataList.add(MapUtils.beanToMap(e));
            String finalTopic = topic;
            if(StringUtils.isBlank(finalTopic)) {
                //默认etl全量同步topic名etl_env_db_table
                finalTopic = SpringUtil.getEnvironment().getProperty("canal.etl-topic-prefix") + "_" + schema  + "_" + table;
            }
            mq.asyncSend(finalTopic, JSON.toJSONString(msg));
        });
    }

    //--------------------------entity对应的都没有强校验内部字段，一般认为自己使用--------------
    /**
     * 没有校验具体字段，entity一般认为是内部操作，校验应该都在DTO层完成了， 但会校验基本null
     */
    public void saveBatchEntity(@NotEmpty @Size(max = BATCH_SIZE) Collection<@NotNull Entity> list){
        dao.insertBatch(list);
    }

    /**
     * 没有校验具体字段，entity一般认为是内部操作，校验应该都在DTO层完成了， 但会校验基本null
     */
    public void saveEntity(@NotNull Entity e) {
        dao.insertSelective(e);
    }
    /**
     * 没有校验任何字段，查询允许null
     */
    public Entity getEntity(Entity e) {
        return dao.getOneByWhere(e);
    }
    /**
     * 内部使用，强制分页，  没有校验任何字段，查询允许null
     */
    public PageResult<Entity> listEntity(Entity e, @Positive Long page, @Positive Long size) {
        PageResult<Entity> pager = new PageResult<Entity>();
        pager.setPage(page);
        pager.setSize(size);
        if(e == null) {
            try {
                e = entityClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                log.error("listEntity error",ex);
            }
        }
        pager.setBody(e);
        pager.setTotal(dao.countByWhere(pager.getBody()));
        if(pager.getTotal() <= 0){
            return pager;
        }
        pager.setRecords(dao.listByWhere(e,pager.getSize(),pager.getOffset()));
        return pager;
    }

    /**
     * 没有校验具体字段，entity一般认为是内部操作，校验应该都在DTO层完成了， 但会校验基本null
     */
    public void editEntity(@NotNull Entity e) {
        dao.updateSelective(e);
    }

    /**
     * 根据条件返回查询总数
     * @return
     */
    public Long maxId(){
       return dao.maxId();
    }

//--------------------------dto对应的会做各种强校验外部使用--------------
    /**
     * 外部调用分页，会作分页保护
     * 分页信息如果不传，默认分页规则
     * @param pager
     * @return
     */
    public PageResult<DTO> listPage(PageResult<DTO> pager) {
        if(null == pager){
            pager = new PageResult<DTO>();
        }
        return list(pager.getBody(),pager.getPage(),pager.getSize());
    }

    /**
     * AOP校验是否为空，代码校验Save和normal两个group
        代码最整洁的写法。AOP和自身校验结合
     */
    public void save(@NotNull DTO dto) {
        validSave(dto);
        saveEntity(convert.dtoToEntity(dto));
    }

    public void saveBatch(@NotEmpty @Size(max = BATCH_SIZE) List<@NotNull DTO> list){
        validListSave(list);
        saveBatchEntity(convert.dtoToEntityList(list));
    }
    /**
     * AOP校验是否为空，代码校验Edit和normal两个group
       代码最整洁的写法。AOP和自身校验结合
     */
    public void edit(@NotNull DTO dto) {
        validEdit(dto);
        editEntity(convert.dtoToEntity(dto));
    }

    /**
     * 不校验空，但如果传了，会校验字段合法性
     */
    public DTO get(DTO dto) {
        validNormal(dto);
        return convert.entityToDto(getEntity(convert.dtoToEntity(dto)));
    }

    /**
     * 内部使用，强制分页，不校验空，但如果传了，会校验字段合法性
     */
    public PageResult<DTO> list(DTO dto,@Positive Long page,@Positive Long size) {
        validNormal(dto);
        PageResult<Entity> result = listEntity(convert.dtoToEntity(dto),page,size);
        PageResult<DTO> pageResult = new PageResult<DTO>();
        pageResult.setPage(result.getPage());
        pageResult.setSize(result.getSize());
        if(result.getTotal() <= 0){
            return pageResult;
        }
        pageResult.setRecords(convert.entityToDtoList(result.getRecords()));
        pageResult.setTotal(result.getTotal());
        return pageResult;
    }

//------------------------------所以根据id和id列表但查询都会校验正数和非空------------------------
    public Entity getEntityById(@NotNull @Positive PK id) {
        try {
            Entity e = entityClass.getDeclaredConstructor().newInstance();
            e.setId(id);
            return dao.getOneByWhere(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public DTO getById(@NotNull @Positive PK id) {
        return convert.entityToDto(getEntityById(id));
    }

    public List<Entity> listEntityByIds(@NotEmpty @Size(max = QUERY_SIZE) Collection<@NotNull @Positive PK> ids) {
        try {
            Entity e = entityClass.getDeclaredConstructor().newInstance();
            e.setIds(ids);
            return dao.listByWhere(e,QUERY_SIZE,0);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<DTO> listByIds(@NotEmpty @Size(max = QUERY_SIZE) Collection<@NotNull @Positive PK> ids) {
        return convert.entityToDtoList(listEntityByIds(ids));
    }

    public void removeById(@NotNull @Positive PK id) {
        try {
            Entity e = entityClass.getDeclaredConstructor().newInstance();
            e.setId(id);
            dao.deleteByWhere(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void removeByIds(@NotEmpty @Size(max = BATCH_SIZE) Collection<@NotNull @Positive PK> ids) {
        try {
            Entity e = entityClass.getDeclaredConstructor().newInstance();
            e.setIds(ids);
            dao.deleteByWhere(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 不保证dto是否为空的校验，如果为空，认为校验通过
     * 所以如果为空校验通过不符合需求，请自行校验是否为空，包括list和内部对象
     * 此方法会校验对象内部具体字段
     */
    public static void validNormal(Object o){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObjNormal(o));
    }
    public static void validSave(Object dto){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObjSave(dto));
    }

    public static void validEdit(Object dto){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObjEdit(dto));
    }
    public static void validEditPart(Object dto){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObj(dto, EditPart.class, Normal.class));
    }
    /**
     * 不保证dtoList是否为空的校验，如果为空，认为校验通过
     * 所以如果为空校验通过不符合需求，请自行校验是否为空，包括list和内部对象
     * 此方法会校验对象内部具体字段
     */
    public static void validListNormal(List<? extends Serializable> dtoList){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObjList(dtoList,Normal.class));
    }

    /**
     * 不保证dtoList是否为空的校验，如果为空，认为校验通过
     * 所以如果为空校验通过不符合需求，请自行校验是否为空，包括list和内部对象
     * 此方法会校验对象内部具体字段
     */
    public static void validListSave(List<? extends Serializable>  dtoList){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObjList(dtoList, Save.class,Normal.class));
    }

    /**
     * 不保证dtoList是否为空的校验，如果为空，认为校验通过
     * 所以如果为空校验通过不符合需求，请自行校验是否为空，包括list和内部对象
     * 此方法会校验对象内部具体字段
     */
    public static void validListEdit(List<? extends Serializable> dtoList){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        assertIfMsg(ValidUtil.validObjList(dtoList, Edit.class,Normal.class));
    }

    public static void assertIfMsg(String msg){
        //具体列表对象可以为空，但如果不为空，会校验具体字段。
        if(StringUtils.isBlank(msg)){
            return;
        }
        throw new RuntimeException(msg);
    }
}
