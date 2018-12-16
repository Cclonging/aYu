package com.alibaba.com.ayu_weather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * http工具类, 用于处理http请求的相关操作
 * @author aYu
 * @date 2018-12-16
 */
public class HttpUtils {

    /**
     * 交给okhttp3发送Http请求
     * @param address
     * @param callback
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
