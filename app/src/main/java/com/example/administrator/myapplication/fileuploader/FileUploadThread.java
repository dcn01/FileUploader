package com.example.administrator.myapplication.fileuploader;

import android.os.*;
import android.os.Process;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Administrator on 2015/9/25 0025.
 */
public class FileUploadThread extends Thread{
    private FileUploadNetwork mFileUploadNetwork;
    private final BlockingQueue<FileUploadRequest> mQueue;
    private volatile boolean mQuit = false;
    public FileUploadThread(FileUploadNetwork fileUploadNetwork,BlockingQueue<FileUploadRequest> queue){
        mFileUploadNetwork = fileUploadNetwork;
        mQueue = queue;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while(!mQuit&&!interrupted()){
            FileUploadRequest request;
            try {
                request = (FileUploadRequest)this.mQueue.take();
            } catch (InterruptedException ex) {
                if(this.mQuit) {
                    return;
                }
                continue;
            }
            if(!request.isCanceled()) {
                mFileUploadNetwork.performRequest(request);
            }
        }
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }
}
