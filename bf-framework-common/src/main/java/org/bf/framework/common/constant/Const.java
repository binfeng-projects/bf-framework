package org.bf.framework.common.constant;

public interface Const {

    //-----------------校验CODE码相关
    int CODE_PERM_ERROR = 403;

    int CODE_NO_LOGIN = 401;

    int CODE_SYSTEM_ERROR = 500;

    Integer CODE_SUC = 0;

    String MSG_SUC = "success";

    String MSG_FAIL = "fail";


    String NOT_NULL_MSG = "cannt not null or empty";




    String DEFAUL_CONTENT_TYPE = "application/octet-stream";

    //--------------------
    //前端传token的key
    String LOG_TRACE_ID = "TRACE_ID";


    String EMPTY = "";

    String BLANK = " ";

    String CACHE_KEY_PARAM_LINK = "_";

    String CACHE_KEY_METHOD_LINK = ".";

    //----------------------线程池相关---------------------------
    int THREAD_CORE = Runtime.getRuntime().availableProcessors() * 2;

    int THREAD_MAX = THREAD_CORE * 2;

    long THREAD_KEEP_ALIVE = 60L;

    int THREAD_QUEUE_SIZE = 5000;

    //----------------------日期相关---------------------------
    String DATA_YMDHMS = "yyyy-MM-dd HH:mm:ss";
    String DATA_YMD = "yyyy-MM-dd";
    String DATA_YM = "yyyy-MM";

    //----------------------convert相关---------------------------
    String CONVERT_ENTITY_DTO = "entityToDto";
    String CONVERT_DTO_ENTITY = "dtoToEntity";
    String CONVERT_DTO_VO = "dtoToVo";

    //----------------------批处理相关---------------------------
    //批量插入或者修改
    int BATCH_SIZE = 2;
    int QUERY_SIZE = 500;
}
