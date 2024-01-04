package org.bf.framework.common.base;

import java.io.Serializable;

public interface PkAble<PK extends Serializable> extends Serializable {
    PK getId();
}
