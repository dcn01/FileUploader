package com.example.administrator.myapplication.fileuploader;

import android.os.Handler;
import android.util.Log;

import com.example.administrator.myapplication.fileuploader.HttpStack.HttpStack;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2015/9/25 0025.
 */
public class FileUploadNetwork {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 20*1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private final HttpStack mhttpStack;

    public FileUploadNetwork(HttpStack httpStack){
        mhttpStack = httpStack;
    }

    public void performRequest(FileUploadRequest request){
        mhttpStack.upload(request);
    }
}
