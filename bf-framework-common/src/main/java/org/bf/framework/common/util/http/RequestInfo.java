package org.bf.framework.common.util.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.bf.framework.common.util.IOUtils;
import org.bf.framework.common.util.JSON;
import org.bf.framework.common.util.StringUtils;
import org.bf.framework.common.util.MapUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
/**
 * Request.Builder 包装类，主要request中的body取参数比较麻烦，log比较困难
 */
@Slf4j
public class RequestInfo implements Serializable {
    private static MediaType MultipartType = MediaType.parse("multipart/form-data");
    private static String MultipartFormKey = "file";

    //不能new 出来，只能通过工厂方法构造
    private RequestInfo(){}
    /**
     * http 底层请求构造
     */
    Request.Builder req;
    /**
     * 请求体，纯打印参数用
     */
    String param;

    /**
     * 标识这是一个下载流，会以流的方式回调
     */
    @Getter
    private Listener.DownloadListener listener;
    /**
     * 回调函数，如果传入，会走异步
     */
    @Getter
    private Callback callback;

    public void setCallback(Callback cb) {
        if (null != cb) {
            this.callback = cb;
        }
    }

    /**
     * 添加header信息
     * @return
     */
    public RequestInfo addHeaders(Map<String, Object> h) {
        checkReq(this);
        if (MapUtils.isEmpty(h)) {
            return this;
        }
        for(Map.Entry<String,Object> e : h.entrySet()){
            addHeader(e.getKey(),e.getValue());
        }
        return this;
    }

    /**
     * 添加header信息
     * @return
     */
    public RequestInfo addHeader(String k, Object v) {
        checkReq(this);
        if(StringUtils.isBlank(k) || v == null){
            return this;
        }
        this.req.header(k, String.valueOf(v));
        return this;
    }

    /**
     * 校验null
     * @return
     */
    public static void checkReq(RequestInfo req) {
        if(req == null || req.req == null){
            throw new RuntimeException("Request.Builder null");
        }
    }
    /**
     * 构造request
     * @param url 请求的目的地址，必须不能为空
     * @param type 枚举值，支持JSON，XML，FORM 不传默认JSON
     * @param body 根据ContentType不同而不同,例如JSON传json字符串或者任何可json序列化的对象
     *              XML支持xml格式的String或者可xml序列化的对象，FORM 支持Map，或者任意可反射的对象
     *             HttpMethod 为GET 和 HEAD 时会忽略ContentType body就是queryParam,可以是Map或者普通对象
     * @param m  如果callback不为空，表示下载流，那么会默认为GET,如果callback为空，会默认为POST。如果传入，则以传入值为准
     * @param listener 如果不为空，则表示这是一个下载流，下载二进制文件，下载成功会回调
     *                 默认以异步方式发起请求。如果用户设置RequestInfo.callback，
     *                 则用户的回调函数优先级更高，完全由用户回调函数掌控处理下载流
     *                 如果listener.async设置false，又未设置RequestInfo.callback，则会以同步方式发起请求，
     *                 有一个带进度条的下载监听器，DownloadProgressListener。可以监听下载进度
     */
    public static RequestInfo genRequest(String url, ContentType type, Object body
            , HttpMethod m, Listener.DownloadListener listener) {
        if(StringUtils.isBlank(url)){
            throw new RuntimeException("genRequest error url empty");
        }
        if(type == null){ type = ContentType.JSON; }
        RequestInfo info = new RequestInfo();
        if(listener != null){
            //加入回调
            info.listener = listener;
            //下载流默认get
            if(m == null){ m = HttpMethod.GET; }
        }else{ //默认post
            if(m == null){ m = HttpMethod.POST; }
        }
        RequestBody b = null;
        if(HttpMethod.GET.equals(m) || HttpMethod.HEAD.equals(m)){
//            GET 和 HEAD方法构造url get参数
            Map<String,Object> param = MapUtils.beanToMap(body);
            url = StringUtils.getQueryString(url,param).toString();
        }else if(body != null){
            if(type.equals(ContentType.JSON)) {
//      JSON方式提交构建
                String jsonStr = body instanceof String ? (String)body : JSON.toJSONString(body);
                b = RequestBody.create(type.getType(),jsonStr);
                info.param = jsonStr;
            }else if(type.equals(ContentType.XML)){
//      XML方式提交构建
                String xmlStr = body instanceof String ? (String)body : JSON.beanToXml(body);
                b = RequestBody.create(type.getType(),xmlStr);
                info.param = xmlStr;
            }else if(type.equals(ContentType.FORM)){
//      表单方式提交构建
                Map<String,Object> param = MapUtils.beanToMap(body);
                FormBody.Builder fb = new FormBody.Builder();
                if (MapUtils.isNotEmpty(param)) {
                    for (Map.Entry<String, Object> e : param.entrySet()) {
                        if(StringUtils.isBlank(e.getKey())|| e.getValue() == null){
                            continue;
                        }
                        fb.add(e.getKey(),String.valueOf(e.getValue()));
                    }
                    info.param = JSON.toJSONString(body);
                }
                b = fb.build();
            }
        }
        info.req = new Request.Builder().url(url).method(m.name(),b);
        return info;
    }

    /**
     * 构造request 主要是body,head自行构建
     * @param url 请求的目的地址
     * @param body Multipart文件流支持三种入参，分别是File，byte[] 或者InputStream
     * @param fileName 文件名
     * @param formParam 除了传文件外的其他表单参数，可为空，k,v形式
     * @param listener 进度条监听器，如果传入，会回传文件上传进度，默认以异步方式发起请求。
     *                 默认会用logCallBack,如果用户设置RequestInfo.callback，
     *                 则用户的回调函数优先级更高。如果listener.async设置false，又未
     *                 设置RequestInfo.callback，则会以同步方式发起请求，不管同步异步，进度条都生效
     */
    public static RequestInfo genMultipartRequest(String url, Object body,String fileName,
                                                  Map<String,Object> formParam, Listener.ProgressListener listener) {
        if(StringUtils.isBlank(fileName) || body == null || StringUtils.isBlank(url)){
            throw new RuntimeException("genMultipartRequest error param empty");
        }
        MultipartBody.Builder b = new MultipartBody.Builder();
        b.setType(MultipartBody.FORM);
        if(body instanceof byte[]){
            //在此处添加多个requestBody实现多文件上传
            b.addFormDataPart(MultipartFormKey, fileName, RequestBody.create(MultipartType,(byte[])body));
        }else if(body instanceof File){
            b.addFormDataPart(MultipartFormKey, fileName, RequestBody.create(MultipartType,(File)body));
        }else if(body instanceof InputStream){
            try {
                byte[] file = IOUtils.toByteArray((InputStream)body);
                b.addFormDataPart(MultipartFormKey, fileName, RequestBody.create(MultipartType,file));
            }catch (Exception e){
                log.error("genMultipartRequest IOException url({})",url,e);
                throw new RuntimeException("genMultipartRequest IOException");
            }
        }else{
            throw new RuntimeException("Multipart body only File,byte[],InputStream is ok");
        }
        RequestInfo info = new RequestInfo();
        if(MapUtils.isNotEmpty(formParam)){
//            for (Map.Entry<String, Object> e : formParam.entrySet()) {
//                if(StringUtils.isBlank(e.getKey())|| e.getValue() == null){
//                    continue;
//                }
//                b.addFormDataPart(e.getKey(),String.valueOf(e.getValue()));
//            }
            info.param = JSON.toJSONString(formParam);
            b.addFormDataPart("json",info.param);
        }
        Request.Builder req = new Request.Builder().url(url);
        info.req = req;
        if(null != listener){
            //用包装过的支持进度条的Body
            req.post(new ProgressRequestBody(b.build(),listener));
            if(listener.async()){
                //添加默认回调函数，以异步方式执行，可以覆盖
                info.setCallback(HttpUtil.logCallBack);
            }
        }else{
            req.post(b.build());
        }
        return info;
    }

    @Getter
    @AllArgsConstructor
    public enum HttpMethod {
        DELETE, GET, HEAD, PATCH, POST, PUT
    }

    @Getter
    @AllArgsConstructor
    public enum ContentType {
        JSON(MediaType.parse("application/json; charset=utf-8")),
        XML(MediaType.parse("application/xml; charset=utf-8")),
        FORM(MediaType.parse("application/x-www-form-urlencoded"));
        private MediaType type;
    }
}
