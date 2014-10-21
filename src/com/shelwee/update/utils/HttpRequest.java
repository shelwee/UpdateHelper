package com.shelwee.update.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * 
 * @author ShelWee
 *
 */
public class HttpRequest {

    public static InputStream get(String url){
        try {
            URL urlPath = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlPath.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setReadTimeout(3000);
            httpURLConnection.connect();
            InputStream inputStream = null;
            if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                inputStream = httpURLConnection.getInputStream();
            }
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TimeOut","the connection is timeout, maybe the server was closed.");
            return null;
        }
    }
}
