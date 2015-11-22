package com.example.administrator.myapplication.fileuploader.HttpStack;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.example.administrator.myapplication.fileuploader.ExecutorDelivery;
import com.example.administrator.myapplication.fileuploader.FileUploadRequest;
import com.example.administrator.myapplication.fileuploader.MultipartEntity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Administrator on 2015/10/8 0008.
 */
public class OriginalStack extends HttpStack{
    private static final int TIME_OUT = 20*1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private final String CONTENT_TYPE = "Content-Type: ";
    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();
    /**
     * 换行符
     */
    private final String NEW_LINE_STR = "\r\n";

    private final String CONTENT_DISPOSITION = "Content-Disposition: ";
    /**
     * 文本参数和字符集
     */
    private final String TYPE_TEXT_CHARSET = "text/plain; charset=UTF-8";

    /**
     * 字节流参数
     */
    private final String TYPE_OCTET_STREAM = "application/octet-stream";
    /**
     * 二进制参数
     */
    private final byte[] BINARY_ENCODING = "Content-Transfer-Encoding: binary\r\n\r\n".getBytes();
    /**
     * 文本参数
     */
    private final byte[] BIT_ENCODING = "Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes();

    /**
     * 分隔符
     */
    private String mBoundary = null;
    /**
     * 输出流
     */
    private DataOutputStream mOutputStream;
    StringBuilder msb = new StringBuilder();
    public OriginalStack(ExecutorDelivery delivery) {
        super(delivery);
        this.mBoundary = generateBoundary();
    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection)url.openConnection();
    }

    private HttpURLConnection openConnection(FileUploadRequest request) throws IOException {
        HttpURLConnection conn = this.createConnection(new URL(request.url));
        int timeoutMs = TIME_OUT;
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true); //允许输出流
        conn.setRequestMethod("POST"); //请求方式
        conn.setRequestProperty("Charset", CHARSET);//设置编码
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + mBoundary);
        return conn;
    }


    @Override
    public void upload(FileUploadRequest request) {
        MultipartEntity m = new MultipartEntity();
        try {
            HttpURLConnection conn = null;
            conn = openConnection(request);
            if (!request.params.isEmpty()||!request.mFiles.isEmpty()) {
                //当文件不为空，把文件包装并且上传
                mOutputStream = new DataOutputStream(conn.getOutputStream());
                for(Map.Entry<String,String> entry:request.params.entrySet()){
                    addStringPart(entry.getKey(),entry.getValue());
                }

                for(Map.Entry<String,File> entry:request.mFiles.entrySet()) {
                    Log.i("cky", "key=" + entry.getKey());
                    addFilePart(entry.getKey(), entry.getValue());
                }

                writeToEnd();
                Log.i("cky",msb.toString());
                //保证百分百进度
                mDelivery.publishProgress(1.00, request.mFileUploadListener);
                //
                // 获取响应码 200=成功
                // 当响应成功，获取响应的流
                //
                StringBuffer sb = new StringBuffer();
                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Log.i("cky","res="+sb.toString());
                mDelivery.finishUpload(code, sb.toString(), conn.getHeaderFields(), request.mFileUploadListener);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToEnd(){
        try {
            mOutputStream.write(NEW_LINE_STR.getBytes());//一定还有换行
            // 参数最末尾的结束符
            final String endString = "--" + mBoundary + "--\r\n";
            // 写入结束符
            mOutputStream.write(endString.getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(mOutputStream!=null){
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 添加文件参数,可以实现文件上传功能
     *
     * @param key
     * @param file
     */
    public void addFilePart(final String key, final File file){
        InputStream fin = null;
        try {
            writeFirstBoundary();
            final String type = CONTENT_TYPE + TYPE_OCTET_STREAM + NEW_LINE_STR;
            mOutputStream.write(getContentDispositionBytes(key, file.getName()));
            msb.append(type);
            mOutputStream.write(type.getBytes());
            msb.append("Content-Transfer-Encoding: binary\r\n\r\n");
            mOutputStream.write(BINARY_ENCODING);
            fin = new FileInputStream(file);
            final byte[] tmp = new byte[4096];
            int len = 0;
            while ((len = fin.read(tmp)) != -1) {
                mOutputStream.write(tmp, 0, len);
            }
            mOutputStream.write(NEW_LINE_STR.getBytes());
            msb.append("二进制数据"+NEW_LINE_STR);
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            closeSilently(fin);
        }
    }

    /**
     * 生成分隔符
     *
     * @return
     */
    private final String generateBoundary() {
        final StringBuffer buf = new StringBuffer();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buf.toString();
    }

    /**
     * 参数开头的分隔符
     *
     * @throws IOException
     */
    private void writeFirstBoundary() throws IOException {
        msb.append(("--" + mBoundary + "\r\n"));
        mOutputStream.write(("--" + mBoundary + "\r\n").getBytes());
    }

    /**
     * 添加文本参数
     *
     * @param key
     * @param value
     */
    public void addStringPart(final String paramName, final String value) {
        writeToOutputStream(paramName, value.getBytes(), TYPE_TEXT_CHARSET, BIT_ENCODING, "");
    }

    /**
     * 将数据写入到输出流中
     *
     * @param key
     * @param rawData
     * @param type
     * @param encodingBytes
     * @param fileName
     */
    private void writeToOutputStream(String paramName, byte[] rawData, String type,
                                     byte[] encodingBytes,
                                     String fileName) {
        try {
            writeFirstBoundary();
            mOutputStream
                    .write(getContentDispositionBytes(paramName, fileName));
            msb.append((CONTENT_TYPE + type + NEW_LINE_STR+NEW_LINE_STR));
            mOutputStream.write((CONTENT_TYPE + type + NEW_LINE_STR).getBytes());
            mOutputStream.write(encodingBytes);
            msb.append(rawData);
            mOutputStream.write(rawData);
            msb.append(NEW_LINE_STR);
            mOutputStream.write(NEW_LINE_STR.getBytes());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getContentDispositionBytes(String paramName, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONTENT_DISPOSITION + "form-data; name=\"" + paramName + "\"");
        // 文本参数没有filename参数,设置为空即可
        if (!TextUtils.isEmpty(fileName)) {
            stringBuilder.append("; filename=\""
                    + fileName + "\"");
        }
        msb.append(stringBuilder.toString()+NEW_LINE_STR);
        return stringBuilder.append(NEW_LINE_STR).toString().getBytes();
    }

    /**
     * 添加二进制参数, 例如Bitmap的字节流参数
     *
     * @param key
     * @param rawData
     */
    public void addBinaryPart(String paramName, final byte[] rawData) {
        writeToOutputStream(paramName, rawData, TYPE_OCTET_STREAM, BINARY_ENCODING, "no-file");
    }

    private void closeSilently(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
