package org.bf.framework.common.base;

public interface IdStringEnum extends PkAble<String>,RemarkAble{
    static <E extends IdStringEnum> E of(String id, Class<E> eClass){
        if(id == null || id.isEmpty()){
            return null;
        }
        E[] enumConstants = eClass.getEnumConstants();
        for (E e : enumConstants) {
            if (id.equals(e.getId())){
                return e;
            }
        }
        return null;
    }
}
