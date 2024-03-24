package org.bf.framework.common.util.valid;


import org.bf.framework.common.base.BaseDTO;
import org.bf.framework.common.util.CollectionUtils;
import org.bf.framework.common.util.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 校验工具类，支持，类，属性，属性值等各个维度校验
 * 所有继承{@link BaseDTO}且标注了JSR validation注解的的类自带校验基础的校验功能
 */
public interface ValidUtil{
//-----------------------------------this对象校验------------------------------------
    String EMPTY = "";
    /**
     * 校验对象自身
     */
    default String validThis(Class<?>...groups) {
        return validObj(this,groups);
    }
    /**
     * Normal校验，只校验Normal组，也就是允许为空，但是如果不为空，必须校验规范性
     */
    default String validNormal() {
        return validObjNormal(this);
    }
    /**
     * save校验，会带上Save和Normal两个组
     */
    default String validSave() {
        return validObjSave(this);
    }
    /**
     * edit校验，会带上Edit和Normal两个组
     */
    default String validEdit() {
        return validObjEdit(this);
    }
//-----------------------------------this的属性校验------------------------------------
    /**
     * 校验对象自身的某个属性,自定义组
     */
    default String validProp(String propertyName, Class<?>...groups) {
        return validObjProp(this,propertyName,groups);
    }
    /**
     * 校验对象自身的多个属性,特定组
     */
    default String validProps(Class<?> g,String... propNames) {
        return validObjProps(this,g,propNames);
    }
    /**
     * 多个属性的Normal校验，只校验Normal组，也就是允许为空，但是如果不为空，必须校验规范性
     */
    default String validPropNormal(String... propNames) {
        return validObjPropNormal(this,propNames);
    }
    /**
     * 多个属性的save校验，会带上Save和Normal两个组
     */
    default String validPropSave(String... propNames) {
        return validObjPropSave(this,propNames);
    }
    /**
     * 多个属性的edit校验，会带上Edit和Normal两个组
     */
    default String validPropEdit(String... propNames) {
        return validObjPropEdit(this,propNames);
    }
//-----------------------------------this属性的值的校验------------------------------------
    /**
     * 校验对象自身某个属性的值,自定义组
     */
    default String validValue(String propertyName,Object value,Class<?>...groups) {
        return validObjValue(this,propertyName,value,groups);
    }
    /**
     * 校验对象自身某个属性的多个属性,特定组
     */
    default String validValues(Class<?> g,String propName,Collection<? extends Serializable> values) {
        return validObjValues(this,g,propName,values);
    }
    /**
     * 某个属性的多个值Normal校验，只校验Normal组，也就是允许为空，但是如果不为空，必须校验规范性
     */
    default String validValueNormal(String propName,Collection<? extends Serializable> values) {
        return validObjValueNormal(this,propName,values);
    }
    /**
     * 某个属性的多个值save校验，会带上Save和Normal两个组
     */
    default String validValueSave(String propName,Collection<? extends Serializable> values) {
        return validObjValueSave(this,propName,values);
    }
    /**
     * 某个属性的多个值edit校验，会带上Edit和Normal两个组
     */
    default String validValueEdit(String propName,Collection<? extends Serializable> values) {
        return validObjValueEdit(this,propName,values);
    }
//-----------------------------------任意对象校验------------------------------------
    /**
     * 校验对象
     * @param groups 校验组
     * @return String 如果是null或者empty，则表示校验通过，否则就是错误信息
     */
    static String validObj(Object o, Class<?>... groups) {
        return validO(ValidatorFactory.getInstance(),o,groups);
    }
    /**
     * 校验对象列表,外部校验整体是否为空
     * @param groups 校验组
     * @return String 如果是null或者empty，则表示校验通过，否则就是错误信息
     */
    static String validObjList(List<? extends Serializable> objList, Class<?>... groups) {
        if(CollectionUtils.isEmpty(objList)){
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objList.size(); i++){
            String msg = validObj(objList.get(i),groups);
            if(StringUtils.isBlank(msg)){
                continue;
            }
            sb.append("----------index : " + i +"---------error\n");
            sb.append(msg);
        }
        return sb.toString();
    }
    /**
     * Normal校验，只校验Normal组，也就是允许为空，但是如果不为空，必须校验规范性
     */
    static String validObjNormal(Object o) {
        return validObj(o,Normal.class);
    }
    /**
     * save校验，会带上Save和Normal两个组
     */
    static String validObjSave(Object o) {
        return validObj(o,Save.class,Normal.class);
    }
    /**
     * edit校验，会带上Edit和Normal两个组
     */
    static String validObjEdit(Object o) {
        return validObj(o,Edit.class,Normal.class);
    }
//-----------------------------------对象的属性校验------------------------------------
    /**
     * 校验bean的某一个属性
     * @param obj          bean
     * @param propertyName 属性名称
     */
    static String validObjProp(Object obj, String propertyName, Class<?>...groups) {
        return validP(ValidatorFactory.getInstance(),obj, propertyName,groups);
    }
    /**
     * 校验对象自身的多个属性,特定组
     */
    static String validObjProps(Object obj,Class<?> g,String... propNames) {
        if(StringUtils.allBlank(propNames)){
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (String p : propNames){
            String msg = validObjProp(obj,p,g);
            if(StringUtils.isBlank(msg)){
                continue;
            }
            sb.append(msg);
        }
        return sb.toString();
    }
    /**
     * 多个属性的Normal校验，只校验Normal组，也就是允许为空，但是如果不为空，必须校验规范性
     */
    static String validObjPropNormal(Object obj, String... propNames) {
        return validObjProps(obj,Normal.class,propNames);
    }
    /**
     * 多个属性的save校验，会带上Save和Normal两个组
     */
    static String validObjPropSave(Object obj, String... propNames) {
        return validObjProps(obj,Save.class,propNames) + validObjPropNormal(propNames);
    }
    /**
     * 多个属性的edit校验，会带上Edit和Normal两个组
     */
    static String validObjPropEdit(Object obj, String... propNames) {
        return validObjProps(obj,Edit.class,propNames) + validObjPropNormal(propNames);
    }
//-----------------------------------对象属性的值的校验------------------------------------
    /**
     * 校验的某一个值,可以具体实例，也可以是一个Class
     * @param propertyName 属性名称
     * @param value 属性值
     */
    static String validObjValue(Object o, String propertyName, Object value, Class<?>... groups) {
        return validV(ValidatorFactory.getInstance(),o, propertyName,value,groups);
    }

    /**
     * 校验对象自身某个属性的多个属性,特定组
     */
    static String validObjValues(Object o, Class<?> g,String propName,Collection<? extends Serializable> values) {
        if(CollectionUtils.allEmpty(values)){
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (Object v : values){
            String msg = validObjValue(o,propName,v,g);
            if(StringUtils.isBlank(msg)){
                continue;
            }
            sb.append(msg);
        }
        return sb.toString();
    }
    /**
     * 某个属性的多个值Normal校验，只校验Normal组，也就是允许为空，但是如果不为空，必须校验规范性
     */
    static String validObjValueNormal(Object o, String propName,Collection<? extends Serializable> values) {
        return validObjValues(o,Normal.class,propName,values);
    }
    /**
     * 某个属性的多个值save校验，会带上Save和Normal两个组
     */
    static String validObjValueSave(Object o,String propName,Collection<? extends Serializable> values) {
        return validObjValues(o,Save.class,propName,values) + validObjValueNormal(o,propName,values);
    }
    /**
     * 某个属性的多个值edit校验，会带上Edit和Normal两个组
     */
    static String validObjValueEdit(Object o,String propName,Collection<? extends Serializable> values) {
        return validObjValues(o,Edit.class,propName,values) + validObjValueNormal(o,propName,values);
    }

//--------------------------------最底层的校验实现，支持任何对象，任何自定义组-----------------------
    /**
     * 返回null或者""表示没有任何错误，否则就是错误提示
     */
    /**
     * 校验对象
     * @param obj bean
     * @param groups 校验组
     * @return String 如果是null，则表示校验通过，否则就是错误信息
     */
    static String validO(Validator v, Object obj, Class<?>... groups) {
        if(CollectionUtils.hasEmpty(v,obj)){
            return EMPTY;
        }
        return genMsg(v.validate(obj,groups));
    }
    /**
     * 校验bean的某一个属性
     * @param obj          bean
     * @param propertyName 属性名称
     */
    static String validP(Validator v,Object obj, String propertyName, Class<?>...groups) {
        if(CollectionUtils.hasEmpty(v,obj,propertyName)){
            return EMPTY;
        }
        return genMsg(v.validateProperty(obj, propertyName,groups));
    }

    /**
     * 校验实例或者class的某一个值
     * @param o 可以是一个具体对象，也可以是一个Class
     * @param propertyName 属性名称
     * @param value 属性值
     */
    static String validV(Validator v, Object o, String propertyName, Object value, Class<?>... groups) {
        if(CollectionUtils.hasEmpty(null,v,o,propertyName,value)){
            return EMPTY;
        }
        Class<?> c = o instanceof Class ? (Class<?>) o : o.getClass();
        return genMsg(v.validateValue(c, propertyName,value,groups));
    }
    /**
     * 返回null或者""表示没有任何错误，否则就是错误提示
     */
    static String genMsg(Set<? extends ConstraintViolation<?>> vrs) {
        if (CollectionUtils.isEmpty(vrs)) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> vr : vrs) {
            sb.append(vr.getPropertyPath().toString()).append(" : ").append(vr.getMessage()).append("\n");
        }
        return sb.toString();
    }
}
