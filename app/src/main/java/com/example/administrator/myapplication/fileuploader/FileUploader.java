package com.example.administrator.myapplication.fileuploader;

import android.os.Handler;
import android.os.Looper;

import com.example.administrator.myapplication.fileuploader.HttpStack.HttpStack;
import com.example.administrator.myapplication.fileuploader.HttpStack.OriginalStack;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2015/9/26 0026.
 */
public class FileUploader {
    //用于生成每个上传任务的唯一序号
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    private FileUploadNetwork mFileUploadNetwork;
    private final BlockingQueue<FileUploadRequest> mQueue;
    private FileUploadThread[] mFileUploadThread;

    public FileUploader(int threadSize){
        this(threadSize,
                new OriginalStack(new ExecutorDelivery(new Handler(Looper.getMainLooper()))));
    }

    public FileUploader(int threadSize,HttpStack stack){
        mQueue = new PriorityBlockingQueue<FileUploadRequest>();;
        mFileUploadNetwork = new FileUploadNetwork(stack);
        mFileUploadThread = new FileUploadThread[threadSize];
    }

    public void start(){
        for(int i=0;i<mFileUploadThread.length;i++){
            mFileUploadThread[i] = new FileUploadThread(mFileUploadNetwork,mQueue);
            mFileUploadThread[i].start();
        }
    }

    public void upload(FileUploadRequest request){
        request.setSequence(getSequenceNumber());
        mQueue.add(request);
    }

    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    public interface RequestFilter {
        public boolean apply(FileUploadRequest request);
    }

    public void cancelAll(RequestFilter filter) {
        synchronized (mQueue) {
            for (FileUploadRequest request : mQueue) {
                if (filter.apply(request)) {
                    request.cancel();
                }
            }
        }
    }

    public void cancelAll(final Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        }
        cancelAll(new RequestFilter() {
            @Override
            public boolean apply(FileUploadRequest request) {
                return request.getTag() == tag;
            }
        });
    }

    public void stop() {
        for (int i = 0; i < mFileUploadThread.length; i++) {
            if (mFileUploadThread[i] != null) {
                mFileUploadThread[i].quit();
            }
        }
    }

    //取消单独的请求
    void finish(FileUploadRequest request) {
        synchronized (mQueue) {
            mQueue.remove(request);
        }
    }
}
