package com.shelwee.update.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * Created by Shelwee on 14-5-8.
 */
public class HttpRequest {

    private String url;

    public HttpRequest(String url) {
        this.url = url;
    }

    private HttpURLConnection buildConnection() throws IOException {
        URL urlPath = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlPath.openConnection();
        httpURLConnection.setConnectTimeout(8000);
        httpURLConnection.setReadTimeout(5000);
        return httpURLConnection;
    }

    public int getContentLength(){
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = buildConnection();
            httpURLConnection.connect();
            int length = 0;
            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                length = httpURLConnection.getContentLength();
            }
            return length;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Request Exception","the connection is timeout or maybe the server was closed.");
            return 0;
        }
    }

    public InputStream get(){
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            httpURLConnection = buildConnection();
            httpURLConnection.connect();
            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                inputStream = httpURLConnection.getInputStream();
            }
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Request Exception","the connection is timeout or maybe the server was closed.");
            return null;
        }
    }
}
