package com.alibaba.com.ayu_weather.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.alibaba.com.ayu_weather.MainActivity;
import com.alibaba.com.ayu_weather.gson.Weather;
import com.alibaba.com.ayu_weather.util.HttpUtils;
import com.alibaba.com.ayu_weather.util.JSONUtil;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private static final String PIC_URL = "http://guolin.tech/api/bing_pic";

    private static final String WEATHER_URL = "http://guolin.tech/api/weather?cityid=";

    private static final String KEY = "&key=66e75d6f413a40c4a95e18517ec5b058";

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBackground();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //8h的毫秒数, 设定每隔8小时自动更新数据
        int anHour = 28800000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = preferences.getString("weather", null);
        if (!Objects.isNull(weatherStr)) {
            Weather weather = JSONUtil.handleWeatherRespone(weatherStr);
            String weatherId = weather.basic.weatherId;
            final String weather_url = WEATHER_URL + weatherId + KEY;
            HttpUtils.sendOkHttpRequest(weather_url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }


                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = JSONUtil.handleWeatherRespone(responseText);
                    if (!Objects.isNull(weather) && Objects.equals("ok", weather.status)) {
                        SharedPreferences.Editor editor =
                                PreferenceManager.
                                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void updateBackground() {
        HttpUtils.sendOkHttpRequest(PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                if (!Objects.isNull(responseText)) {
                    SharedPreferences.Editor editor =
                            PreferenceManager.
                                    getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic", responseText);
                    editor.apply();
                }
            }
        });
    }
}
