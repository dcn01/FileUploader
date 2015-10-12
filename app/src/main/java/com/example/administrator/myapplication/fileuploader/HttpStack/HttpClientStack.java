package com.example.administrator.myapplication.fileuploader.HttpStack;

import android.os.Handler;
import android.os.Looper;

import com.example.administrator.myapplication.fileuploader.ExecutorDelivery;
import com.example.administrator.myapplication.fileuploader.FileUploadRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/9 0009.
 */
public class HttpClientStack extends HttpStack{

    public HttpClientStack() {
        super(new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public HttpClientStack(ExecutorDelivery delivery) {
        super(delivery);
    }

    @Override
    public void upload(FileUploadRequest request) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            //设置通信协议版本
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

            HttpPost httppost = new HttpPost(request.url);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(Charset.forName("UTF-8"));//设置请求的编码格式
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式

            for(Map.Entry<String,File> entry:request.mFiles.entrySet()){
                builder.addBinaryBody(entry.getKey(), entry.getValue());
            }

            for(Map.Entry<String,String> entry:request.params.entrySet()) {
                builder.addTextBody(entry.getKey(),entry.getValue());
            }

            httppost.setEntity(builder.build());
            System.out.println("executing request " + httppost.getRequestLine());

            HttpResponse response = null;
            response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            System.out.println(response.getStatusLine());//通信Ok
            String res;
            if (resEntity != null) {
                //System.out.println(EntityUtils.toString(resEntity,"utf-8"));
                res= EntityUtils.toString(resEntity, "utf-8");
            }
            if (resEntity != null) {
                resEntity.consumeContent();
            }

            httpclient.getConnectionManager().shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
