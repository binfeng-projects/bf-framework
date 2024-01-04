package org.bf.framework.common.util.http;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

import java.io.IOException;

/**
 * 带进度条的文件上传
 */
public class ProgressRequestBody extends RequestBody {

    private MultipartBody requestBody;

    private Listener.ProgressListener listener;

    private ProgressRequestBody(MultipartBody rb) {
        this.requestBody = rb;
    }

    public ProgressRequestBody(MultipartBody rb, Listener.ProgressListener l) {
        this(rb);
        this.listener = l;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        ForwardingSink proxySink = new ForwardingSink(sink) {
            long totalBytesWrite = 0L;
            long percent = 0L;
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                totalBytesWrite += byteCount;
                if (listener != null) {
                    //百分比整数才更新
                    long newPercent = totalBytesWrite * 100 / contentLength();
                    if (newPercent > percent) {
                        this.percent = newPercent;
                        listener.onProgress(contentLength(), totalBytesWrite);
                    }
                }
                super.write(source, byteCount);
            }
        };
        BufferedSink bs = Okio.buffer(proxySink);
        requestBody.writeTo(bs);
        bs.flush();
    }
}
