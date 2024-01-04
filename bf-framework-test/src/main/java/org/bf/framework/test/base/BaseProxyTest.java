package org.bf.framework.test.base;

import org.bf.framework.boot.base.BaseDaoProxy;
import org.bf.framework.common.base.BaseDTO;
import org.bf.framework.common.base.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseProxyTest<PK extends Number,DTO extends BaseDTO<PK>,Entity extends BaseEntity<PK>>  implements BaseCoreTest{
    @Autowired
    protected BaseDaoProxy<PK,DTO,Entity> daoProxy;

}
