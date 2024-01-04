package org.bf.framework.common.util.valid;

import lombok.experimental.Delegate;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;

public class ValidCollection<E> implements Collection<E> {

    @Valid
    @Delegate
    public Collection<E> list = new ArrayList<E>();

    // 一定要记得重写toString方法
    @Override
    public String toString() {
        return list.toString();
    }
}
