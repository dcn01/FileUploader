package com.example.administrator.myapplication.fileuploader;

import android.os.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2015/10/9 0009.
 */
public class ExecutorDelivery {
    private final Executor mResponsePoster;

    public ExecutorDelivery(final Handler handler) {
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    public ExecutorDelivery(Executor executor) {
        mResponsePoster = executor;
    }

    public void publishProgress(final double pro, final FileUploadRequest.FileUploadListener listener){
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                listener.onProgress(pro);
            }
        });
    }

    public void finishUpload(final int code, final String res, final Map<String,List<String>> headers,final FileUploadRequest.FileUploadListener listener){
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                listener.onFinish(code, res, headers);
            }
        });
    }

    public void failUpload(final FileUploadRequest.FileUploadListener listener){
        mResponsePoster.execute(new Runnable() {
            @Override
            public void run() {
                listener.onFail();
            }
        });
    }
}
