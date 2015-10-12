package com.example.administrator.myapplication.fileuploader;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/25 0025.
 */
public class FileUploadRequest implements Comparable<FileUploadRequest>{
    public String url;
    public Map<String,File> mFiles = new HashMap<String,File>();
    public Map<String,String> params = new HashMap<String,String>();
    private boolean mCanceled;
    private Integer mSequence;
    public FileUploadListener mFileUploadListener;
    private Object tag;

    public FileUploadRequest(String url,FileUploadListener fileUploadListener){
        this.url = url;
        mFileUploadListener = fileUploadListener;
    }

    public interface FileUploadListener{
        public void onProgress(double precent);
        public void onFinish(int code, String res, Map<String, List<String>> headers);
        public void onFail();
    }

    public void addParam(String key,String val){
        params.put(key,val);
    }

    public void addFile(String key,File file){
        mFiles.put(key,file);
    }

    public Object getTag() {
        return tag;
    }

    public void cancel() {
        this.mCanceled = true;
    }

    public boolean isCanceled() {
        return this.mCanceled;
    }

    @Override
    public int compareTo(FileUploadRequest other) {
        FileUploadRequest.Priority left = this.getPriority();
        FileUploadRequest.Priority right = other.getPriority();
        return left == right?this.mSequence.intValue() - other.mSequence.intValue():right.ordinal() - left.ordinal();
    }

    public final void setSequence(int sequence) {
        this.mSequence = Integer.valueOf(sequence);
    }

    public final int getSequence() {
        if(this.mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        } else {
            return this.mSequence.intValue();
        }
    }

    public static enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE;

        private Priority() {
        }
    }

    public FileUploadRequest.Priority getPriority() {
        return FileUploadRequest.Priority.NORMAL;
    }
}
