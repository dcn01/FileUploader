# FileUploader
用于Android开发中的文件上传功能，使用多线程方式进行文件上传，可以方便地扩展，使用方式也很简单。
优点如下：
1、实现带参数的文件上传
2、可以批量上传文件
3、实现带进度提示，在可以在监听器中更新UI
4、适用于小型文件的上传

使用方式：
package com.example.administrator.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.administrator.myapplication.fileuploader.FileUploadRequest;
import com.example.administrator.myapplication.fileuploader.FileUploader;
import com.example.administrator.myapplication.fileuploader.HttpStack.HttpClientStack;

import java.io.File;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener{
    Button btn;
    FileUploader fl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
        fl = new FileUploader(2,new HttpClientStack());
        fl.start();
    }

    @Override
    public void onClick(View v) {
        String url = "你的上传地址";
        File f = new File(SDCardUtils.getSDCardPath()+"all123.png");//文件1
        File f2 = new File(SDCardUtils.getSDCardPath()+"hhh.jpg");//文件2
        if(f.exists()){
            //url请求地址
            FileUploadRequest r = new FileUploadRequest(url, new FileUploadRequest.FileUploadListener() {
                @Override
                public void onProgress(double precent) {
                  //更新进度
                }

                @Override
                public void onFinish(int code, String res, Map<String, List<String>> headers) {
                  //上传结束
                }

                @Override
                public void onFail() {
                  //上传失败
                }
            });
            r.addParam("cky", "cky");//添加参数，POST方式
            r.addFile("nini", f);//添加文件
            for(int i=0;i<2;i++){//添加文件
                r.addFile("ee"+i,f2);
            }
            fl.upload(r);//上传文件
        }else{
            Log.i("cky","文件不存在");
        }
    }
}
