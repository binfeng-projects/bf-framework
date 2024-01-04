package org.bf.framework.common.util.http;

import okhttp3.Response;

public interface Listener {
    /**
     * 同步写，还是异步写
     */
    default boolean async() {
        return true;
    }

    /**
     * 上传下载进度条监听，可以自行实现往redis写等
     * async标识是异步发起请求还是同步，异步可以被RequetInfo.callback覆盖
     */
    interface ProgressListener extends Listener{
        /**
         * 进度条回调方法
         */
        void onProgress(long total, long progress);
    }

    /**
     * 标识这是一个下载流,并会在成功的时候回调，只有成功会回调，失败会打印异常信息
     * async标识是异步发起请求还是同步，异步可以被RequetInfo.callback覆盖
     */
    interface DownloadListener extends Listener {

        /**
         * 下载回调这个response，业务可以自行处理下载流
         * 可以简单返回一个msg,例如"SUCCESS"，ERROR等，会打印在日志里
         */
        String onDownload(Response resp);
    }

    /**
     * 带进度条的下载流
     */
    interface DownloadProgressListener extends DownloadListener,ProgressListener {

    }
}

