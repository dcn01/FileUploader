package com.example.administrator.myapplication.fileuploader.HttpStack;

import android.os.Handler;

import com.example.administrator.myapplication.fileuploader.ExecutorDelivery;
import com.example.administrator.myapplication.fileuploader.FileUploadRequest;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/8 0008.
 */
public abstract class HttpStack {
    public static final String TAG = "mfile";
    public final ExecutorDelivery mDelivery;
    HttpStack(ExecutorDelivery delivery){
        mDelivery = delivery;
    }
    public abstract void upload(FileUploadRequest request);
}
