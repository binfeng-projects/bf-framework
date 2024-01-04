package org.bf.framework.common.util.http;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import java.io.IOException;

/**
 * 包装的响体，处理进度
 */
public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;

    private Listener.ProgressListener listener;

    private BufferedSource bufferedSource;

    private ProgressResponseBody(ResponseBody resp) {
        this.responseBody = resp;
    }

    public ProgressResponseBody(ResponseBody resp, Listener.ProgressListener l) {
        this(resp);
        this.listener = l;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            ForwardingSource proxySouce = new ForwardingSource(responseBody.source()) {
                //当前读取字节数
                long totalBytesRead = 0L;
                long percent = 0L;
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (listener != null) {
                        //百分比整数才更新
                        long newPercent = totalBytesRead * 100 / contentLength();
                        if (newPercent > percent) {
                            this.percent = newPercent;
                            listener.onProgress(contentLength(), totalBytesRead);
                        }
                    }
                    //回调，如果contentLength()不知道长度，会返回-1
                    return bytesRead;
                }
            };
            bufferedSource = Okio.buffer(proxySouce);
        }
        return bufferedSource;
    }
}