package org.bf.framework.common.util.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.bf.framework.common.util.IOUtils;
import org.bf.framework.common.util.StringUtils;

import javax.net.ssl.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtil {
    private static final long HTTP_TIMEOUT = 20;
    private static final long HTTP_TIMEOUT_LONG = 3600;
    static OkHttpClient httpClient;
    private static EventSource.Factory eventFactory;
    private static final String DEFAULT_API_HOST = "https://api.bf.org/v1/chat/completions";
    private HttpUtil(){}
    static {
        OkHttpClient.Builder bd = new OkHttpClient.Builder();
        setTimeOut(bd,HTTP_TIMEOUT);
        httpClient = bd.retryOnConnectionFailure(true)
//                .addNetworkInterceptor(interceptor) //网络拦截器
//                .connectionSpecs(...) // 设置连接的规格、TLS版本和密码套件等 添加受信证书，最好不要去设置
//                .protocols()  // 设置使用的协议，目前支持http1.1和http2，不能包含空和http1.0。一般不会设置
//                .socketFactory(new SocketFactory() {...}) // 使用定制的用于http请求的套接字
//                .proxy(...) // 设置单个代理
//                .proxyAuthenticator(...)  // 设置代理验证
//                .proxySelector(...) // 为不同的链接设置不同的代理
//                .authenticator()//
//                .cookieJar(new CookieJar() {
//                    @Override
//                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
//                        // response时保存cookie
//                    }
//                    @NotNull
//                    @Override
//                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
//                        // request发送前，将cookie加到request中
//                        return null;
//                    }
//                })
                // 服务器对下发的资源做GZip操作，而此时就没有相应的content-length， 这样强迫服务器不走压缩。
                //没啥用,服务端返回content-length
//                .addInterceptor(chain ->  chain.proceed(chain.request().newBuilder().addHeader("Accept-Encoding", "identity").build()))
                .pingInterval(30, TimeUnit.SECONDS) // 设置ping检测网络连通性的间隔。默认为0
                .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })// 定义连接池，最多有五个空闲连接，每个空闲连接最多保持6分钟
                .connectionPool(new ConnectionPool(5, 6, TimeUnit.MINUTES))
                .followRedirects(true)       // 允许http重定向
                .followSslRedirects(false)   // 截断https的重定向
                // 指定分发器，即异步执行http请求时的线程池，http响应的回调也是在此线程池的线程中执行
//                .dispatcher(new Dispatcher(MDCUtil.createDefaultPoolExecutor()))
                .build();

//        定制化的OkHttpClient与原实例共享连接池、线程池和公共配置项。OkHttpClient的配置项位于build方法前。
//        OkHttpClient customClient = httpClient.newBuilder().build();
        eventFactory =  EventSources.createFactory(httpClient);

    }
    public static void streamRequest(Request req, EventSourceListener listener){
        streamRequest(eventFactory,req,listener);
    }
    public static void streamRequest(EventSource.Factory fac,Request req, EventSourceListener listener){
        if(fac == null || req == null || listener == null) {
            return;
        }
        fac.newEventSource(req,listener);
    }
    /**
     *
     * @param url 如果url为空，用默认的url
     * @param apiKey 如果apiKey不为空，那么会加入header，兼容微软和gpt两种认证方式
     * @param body 不能为空，外部自己序列化好
     * @return
     */
    public static Request formatStreamRequest(String url,String apiKey, String body){
        if(StringUtils.isBlank(body)){
            throw new RuntimeException("body cannot null");
        }
        String hostUrl = StringUtils.isBlank(url) ? DEFAULT_API_HOST : url;
        Request.Builder builder = new Request.Builder().url(hostUrl);
        if(StringUtils.isNotBlank(apiKey)){
            builder.header("Authorization",apiKey).header("api-key",apiKey); //兼容微软gpt和原生gpt的认证
        }
        builder.header("Content-Type","application/json");
        return builder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),body)).build();
    }

    /**
     * 默认的回调函数，仅打日志 public，对异步调用只想看日志，不处理结果的可使用
     * 成功打印response 失败打印异常，打印url,不打印入参body。
     */
    public static final Callback logCallBack = new Callback() {
        @Override
        public void onFailure(Call call,IOException e) {
            logResult(call,e,null);
        }
        @Override
        public void onResponse(Call call,Response resp) {
            try {
                String httpResult = checkResponse(call.execute()).body().string();
                logResult(call,httpResult,null);
            } catch (Exception e) {
                logResult(call,e,null);
            }
        }
    };
    private static void setTimeOut(OkHttpClient.Builder b, long t) {
//    private static final long CALL_TIMEOUT = 30;
        //连接超时时间,连接超时是在将TCP SOCKET 连接到目标主机时应用的，默认10s
        b.connectTimeout(t, TimeUnit.SECONDS)
                //读取超时时间, 包括TCP SOCKET和Source 和Response的读取IO操作，默认10s
                .readTimeout(t, TimeUnit.SECONDS)
                //写入超时时间，主要指IO的写入操作，默认10s
                .writeTimeout(t, TimeUnit.SECONDS)
                //整个调用时期的超时时间，包括解析DNS、链接、写入请求体、服务端处理、以及读取响应结果
                .callTimeout(t + 10, TimeUnit.SECONDS);
    }
    /**
     * 常用方法，如果需要传入head或者支持HEAD PUT DELETE PATCH等非常用HttpMethod,可以参照本方法分步构建
     * @param url 请求地址
     * @param param 提交参数
     * @param cb 回调，如果传入，会以异步执行
     */
    public static String get(String url,Map<String,Object> param,Callback cb) {
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.FORM,param, RequestInfo.HttpMethod.GET,null);
        req.setCallback(cb);
        return execute(req);
    }

    /**
     * 常用方法，如果需要传入head或者支持HEAD PUT DELETE PATCH等非常用HttpMethod,可以参照本方法分步构建
     * @param url 请求地址
     * @param json 提交参数 json字符串
     * @param cb 回调，如果传入，会以异步执行
     */
    public static String postJSON(String url,String json,Callback cb) {
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.JSON,json, RequestInfo.HttpMethod.POST,null);
        req.setCallback(cb);
        return execute(req);
    }
    public static String putJSON(String url,String json,Callback cb) {
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.JSON,json, RequestInfo.HttpMethod.PUT,null);
        req.setCallback(cb);
        return execute(req);
    }
    /**
     * 常用方法，如果需要传入head或者支持HEAD PUT DELETE PATCH等非常用HttpMethod,可以参照本方法分步构建
     * @param url 请求地址
     * @param xml 提交参数 xml字符串
     * @param cb 回调，如果传入，会以异步执行
     */
    public static String postXML(String url,String xml,Callback cb) {
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.XML,xml,null,null);
        req.setCallback(cb);
        return execute(req);
    }
    /**
     * 常用方法，如果需要传入head或者支持HEAD PUT DELETE PATCH等非常用HttpMethod,可以参照本方法分步构建
     * @param url 请求地址
     * @param param 提交参数
     * @param cb 回调，如果传入，会以异步执行
     */
    public static String postForm(String url,Map<String,Object> param,Callback cb) {
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.FORM,param,null,null);
        req.setCallback(cb);
        return execute(req);
    }

    /**
     * @param url 上传地址
     * @param in 输入流
     * @param fileName 文件名
     * @param cb 回调，如果传入，会以异步执行
     * @return
     */
    public static String upload(String url,InputStream in,String fileName,Callback cb) {
        RequestInfo req = RequestInfo.genMultipartRequest(url,in,fileName,null,null);
        req.setCallback(cb);
        return execute(longTimeOutBuild().build(),req);
    }

    /**
     * @param url 上传地址
     * @param b 字节数组
     * @param fileName 文件名
     * @param cb 回调，如果传入，会以异步执行
     * @return
     */
    public static String upload(String url,byte[] b,String fileName,Callback cb) {
        RequestInfo req = RequestInfo.genMultipartRequest(url,b,fileName,null,null);
        req.setCallback(cb);
        return execute(longTimeOutBuild().build(),req);
    }

    /**
     * @param url 上传地址
     * @param f File文件
     * @param fileName 文件名
     * @param cb 回调，如果传入，会以异步执行
     * @return
     */
    public static String upload(String url,File f,String fileName,Callback cb) {
        RequestInfo req = RequestInfo.genMultipartRequest(url,f,fileName,null,null);
        req.setCallback(cb);
        return execute(longTimeOutBuild().build(),req);
    }

    /**
     * download GET请求居多，如果需要传入head或者支持HEAD PUT DELETE PATCH等非常用HttpMethod,可以参照本方法分步构建
     * @param url 请求地址
     * @param param 提交参数
     * @param listener 下载回调。如果传入DownloadProgressListener。可以监听下载进度
     */
    public static String download(String url, Map<String,Object> param, Listener.DownloadListener listener) {
        if(null == listener){
            throw new RuntimeException("download listener null");
        }
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.FORM,param, RequestInfo.HttpMethod.GET,listener);
        return execute(longTimeOutBuild().build(),req);
    }

    /**
     * download POST JSON请求，如果需要传入head或者支持HEAD PUT DELETE PATCH等非常用HttpMethod,可以参照本方法分步构建
     * @param url 请求地址
     * @param json 提交参数
     * @param listener 下载回调。如果传入DownloadProgressListener。可以监听下载进度
     */
    public static String downloadPost(String url,String json, Listener.DownloadListener listener) {
        if(null == listener){
            throw new RuntimeException("download listener null");
        }
        RequestInfo req = RequestInfo.genRequest(url, RequestInfo.ContentType.JSON,json, RequestInfo.HttpMethod.POST,listener);
        return execute(longTimeOutBuild().build(),req);
    }

    private static OkHttpClient.Builder longTimeOutBuild() {
        OkHttpClient.Builder nb = httpClient.newBuilder();
        setTimeOut(nb,HTTP_TIMEOUT_LONG); //更改超时时间
        return nb;
    }

    public static String execute(RequestInfo req) {
        return execute(null,req) ;
    }

    /**
     * 底层方法
     * 参数比较多，分两部进行
     * @link HttpUtil.genMultipartRequest HttpUtil.genRequest
     * @param req 先用 genMultipartRequest genRequest 构造请求主体，也就是这里的req
     * @param client 可为空，如果传入会添加到会使用自定义的client，一般不要自定义。
     *               除了像需要增加拦截器之类的特殊需求
     */
    public static String execute(OkHttpClient client,RequestInfo req) {
        RequestInfo.checkReq(req);
        if(null == client){
            client = httpClient;
        }
        if(null != req.getListener() && req.getListener() instanceof Listener.DownloadProgressListener){
            //表名是个下载流， 且需要监听回调,重启起一个Clint，加入拦截器
            OkHttpClient.Builder nb = client.newBuilder();
            nb.addNetworkInterceptor(chain ->{
                Response oriResp = chain.proceed(chain.request());
                return oriResp.newBuilder().body(new ProgressResponseBody(oriResp.body(),
                        (Listener.DownloadProgressListener)req.getListener())).build();
            });
            client = nb.build();
        }
        Call call = client.newCall(req.req.build());
        //同步执行
        if(req.getCallback() == null){
            try {
                String result;
                //null != req.listener  说明这是一个文件下载流，需要按照流的方式处理。
                if(null != req.getListener()){
                    result =  processStream(call,req);
                }else{//否则就是个普通的请求，正常返回String
                    result = checkResponse(call.execute()).body().string();
                }
                logResult(call,result,req);
                return result;
            } catch (Exception e) {
                //重新抛出业务异常
                throw logResult(call,e,req);
            }
        }else{ //异步处理，需要用户处理返回信息和日志信息
            call.enqueue(req.getCallback());
        }
        return null;
    }

    private static String processStream(Call call,RequestInfo req) throws Exception{
        //异步下载
        if(req.getListener().async()){
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call,IOException e) {
                    //异步异常打日志
                    logResult(call,e,req);
                }
                @Override
                public void onResponse(Call call,Response resp) {
                    try {
                        checkResponse(resp);
                        String successMsg = req.getListener().onDownload(resp);
                        logResult(call,successMsg,req);
                    }catch (Exception e){
                        //异步异常打日志
                        logResult(call,e,req);
                    }
                }
            });
        }else{//同步下载
            try {
                Response resp = checkResponse(call.execute());
                return req.getListener().onDownload(resp);
            }catch (Exception e){
                //同步异常往外抛，外部会处理
                throw e;
            }
        }
        return null;
    }

    private static RuntimeException logResult(Call call,Object result,RequestInfo req){
        StringBuilder sb = new StringBuilder();
        if(null == req){
            sb.append("async http ");
        }else{
            //download
            if(null != req.getListener()){
                if(req.getListener().async()){
                    sb.append("async");
                }
                sb.append(" download ");
            }else{
                sb.append("http ");
            }
        }
        sb.append(" url(").append(call.request().url()).append(") ");
        if(null != req){
            sb.append("param=(").append(req.param).append(") ");
        }
        if(result instanceof Throwable){
            sb.append(result.getClass().getName());
            return new RuntimeException(sb.toString());
        }else{
            sb.append("result=(").append(result).append(")");
            log.info(sb.toString());
            return null;
        }
    }
    /**
     * 统一处理 response 包括code和是否为空的校验
     */
    private static Response checkResponse(Response resp){
        if(resp == null ||resp.body() == null){
            throw new RuntimeException("http response empty");
        }
        if(!resp.isSuccessful()){
            throw new RuntimeException("http response fail code:" + resp.code());
        }
        log.info("url({}) response success heads {}",resp.request().url(),resp.headers());
        return resp;
    }

    public static String saveFile(Response resp,String filePath){
        InputStream in = resp.body().byteStream();
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            IOUtils.copy(in,out);
            return "success";
        }catch (Exception e){
            log.error("save file error",e);
            return "fail";
        }finally {
            IOUtils.closeQuietly(in,out);
        }
    }

    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     */
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    /**
     * 用于信任所有证书
     */
    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s){
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s){

        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}





