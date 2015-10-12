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
        String url = "http://2.firstword.sinaapp.com/index.php/Index/uploadtest";
        File f = new File(SDCardUtils.getSDCardPath()+"all123.png");
        File f2 = new File(SDCardUtils.getSDCardPath()+"hhh.jpg");
        if(f.exists()){
            FileUploadRequest r = new FileUploadRequest(url, new FileUploadRequest.FileUploadListener() {
                @Override
                public void onProgress(double precent) {

                }

                @Override
                public void onFinish(int code, String res, Map<String, List<String>> headers) {

                }

                @Override
                public void onFail() {

                }
            });
            //r.addParam("cky", "cky");
            r.addFile("nini", f);
            for(int i=0;i<2;i++){
                r.addFile("ee"+i,f2);
            }
            fl.upload(r);
        }else{
            Log.i("cky","sss");
        }
    }
}
