package org.bf.framework.common.base;
public interface IdIntegerEnum extends PkAble<Integer>,RemarkAble{
    static <E extends IdIntegerEnum> E of(Integer id, Class<E> eClass){
        if(id == null){
            return null;
        }
        E[] enumConstants = eClass.getEnumConstants();
        for (E e : enumConstants) {
            if (e.getId().equals(id)){
                return e;
            }
        }
        return null;
    }
}
