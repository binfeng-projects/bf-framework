package org.bf.framework.common.result;

import lombok.experimental.FieldNameConstants;
import org.bf.framework.common.base.IdIntegerEnum;
import org.bf.framework.common.constant.Const;

@FieldNameConstants
public class Result<T> implements Const {


    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    protected T body;

    protected Integer code = CODE_SUC;

    protected String msg = MSG_SUC;
    public static <T> Result<T> of(T model) {
        Result<T> result = new Result<T>();
        result.setBody(model);
        return result;
    }

    public static <T> Result<T> fail(String message){
        Result<T> result = new Result<T>();
        result.setBody(null);
        result.setCode(CODE_SYSTEM_ERROR);
        result.setMsg(message);
        return result;
    }
    public static <T> Result<T> fail(Integer code, String message){
        Result<T> result = new Result<T>();
        result.setBody(null);
        result.setCode(code);
        result.setMsg(message);
        return result;
    }
    public static <T> Result<T> fail(String message,T model){
        Result<T> result = new Result<T>();
        result.setBody(model);
        result.setCode(CODE_SYSTEM_ERROR);
        result.setMsg(message);
        return result;
    }
    public static <T> Result<T> fail(Integer code, String message,T model){
        Result<T> result = new Result<T>();
        result.setBody(model);
        result.setCode(code);
        result.setMsg(message);
        return result;
    }
    public static <T> Result<T> fail(IdIntegerEnum errEnum){
        Result<T> result = new Result<T>();
        result.setBody(null);
        result.setCode(errEnum.getId());
        result.setMsg(errEnum.getRemark());
        return result;
    }
}
