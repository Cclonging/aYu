package com.alibaba.com.ayu_weather;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.com.ayu_weather.gson.Forecast;
import com.alibaba.com.ayu_weather.gson.Weather;
import com.alibaba.com.ayu_weather.service.AutoUpdateService;
import com.alibaba.com.ayu_weather.util.HttpUtils;
import com.alibaba.com.ayu_weather.util.JSONUtil;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String PIC_URL = "http://guolin.tech/api/bing_pic";

    private static final String WEATHER_URL = "http://guolin.tech/api/weather?cityid=";

    private static final String KEY = "&key=66e75d6f413a40c4a95e18517ec5b058";

    private ScrollView weatherLayout;

    private TextView tityCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView apiText;

    private TextView pm15Text;

    private TextView qltText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | layoutParams.flags);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });


        //初始化各个控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        tityCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        apiText = (TextView) findViewById(R.id.api_text);
        pm15Text = (TextView) findViewById(R.id.pm25_text);
        qltText = (TextView) findViewById(R.id.qlt_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);



        //先从缓存里读取数据
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = "";
        if (!Objects.equals("yes", getIntent().getStringExtra("reCommand"))) {
            weatherStr = preferences.getString("weather", null);
        }
        String bingPic = preferences.getString("bing_pic", null);
        if (!Objects.isNull(bingPic)){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        if (!Objects.isNull(weatherStr) && !Objects.equals("",weatherStr)){
            Weather weather = JSONUtil.handleWeatherRespone(weatherStr);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //没有缓存数据,从服务器读取
            mWeatherId = getIntent().getStringExtra("weather_id");
            if (Objects.isNull(mWeatherId)){
                mWeatherId = "CN101190104";
            }
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }else {
                initMediaPlayer(mediaPlayer);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if (!mediaPlayer.isPlaying())
                            mediaPlayer.start();
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {

                    }
                    mediaPlayer.reset();
                    initMediaPlayer(mediaPlayer);
                }
            });

            return true;
        }else if (id == R.id.action_location) {
            requestWeather("CN101190104");
        }

        return super.onOptionsItemSelected(item);
    }

    private void initMediaPlayer(MediaPlayer mediaPlayer) {

        try{
            File file = new File(Environment.getExternalStorageDirectory(),"music.mp3");
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void loadBingPic() {
        HttpUtils.sendOkHttpRequest(PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据获取失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        PreferenceManager.
                                getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示缓存中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        tityCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        //先清楚缓存
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecasts){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            ImageView infoText = (ImageView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            setInfoImage(infoText, forecast.more.info);
            maxText.setText(forecast.temperature.max + "℃");
            minText.setText(forecast.temperature.min + "℃");
            forecastLayout.addView(view);
        }

        if (!Objects.isNull(weather.aqi)){
            apiText.setText(weather.aqi.city.aqi);
            pm15Text.setText(weather.aqi.city.pm25);
            qltText.setText(weather.aqi.city.qlty);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        //开启自动更新数据服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void setInfoImage(ImageView infoText, String info) {
        if (Objects.equals("晴",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.sun));
        }else if (Objects.equals("多云",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.cloud));
        }else if (Objects.equals("阴",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.shade));
        }else if (Objects.equals("小雨",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.small_rain));
        }else if (Objects.equals("中雨",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.mid_rain));
        }else if (Objects.equals("大雨",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.big_rain));
        }else if (Objects.equals("小雪",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.small_snow));
        }else if (Objects.equals("中雪",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.mid_snow));
        }else if (Objects.equals("大雪",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.big_snow));
        }else if (Objects.equals("雾",info)){
            infoText.setImageDrawable(getResources().getDrawable(R.drawable.fog));
        }
    }

    /**
     * 根据id请求城市的天气信息,保存至缓存, 再展示数据
     * @param weatherId
     */
    public void requestWeather(final String weatherId){
        loadBingPic();

        final String weather_url = WEATHER_URL + weatherId + KEY;
        HttpUtils.sendOkHttpRequest(weather_url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据获取失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = JSONUtil.handleWeatherRespone(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!Objects.isNull(weather) && Objects.equals("ok", weather.status)){
                            SharedPreferences.Editor editor =
                                    PreferenceManager.
                                            getDefaultSharedPreferences(MainActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(MainActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

}
