package org.bf.framework.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bf.framework.common.base.IdIntegerEnum;
import org.bf.framework.common.constant.Const;

@Getter
@AllArgsConstructor
public enum DefaultError implements IdIntegerEnum, Const {
    PARAM_EMPTY(10004, "empty param"),
    HTTP_ERROR(10005, "http invoke error"),
    PERM_ERROR(CODE_PERM_ERROR, "no permissoon"),
    NO_LOGIN(CODE_NO_LOGIN, "no login"),
    SYSTEM_ERROR(CODE_SYSTEM_ERROR, "system error"),
    VALIDATE(33001, "validate error");
    private Integer id;
    private String remark;
}
