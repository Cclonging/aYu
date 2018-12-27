package com.alibaba.com.ayu_weather;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.com.ayu_weather.gson.Forecast;
import com.alibaba.com.ayu_weather.gson.Weather;
import com.alibaba.com.ayu_weather.service.AutoUpdateService;
import com.alibaba.com.ayu_weather.util.HttpUtils;
import com.alibaba.com.ayu_weather.util.JSONUtil;
import com.alibaba.com.ayu_weather.util.Snow;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.alibaba.com.ayu_weather.R.color.chatBg;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String PIC_URL = "http://guolin.tech/api/bing_pic";

    private static final String WEATHER_URL = "http://guolin.tech/api/weather?cityid=";

    private static final String KEY = "&key=66e75d6f413a40c4a95e18517ec5b058";

    private static final int CHOOSE_PHOTO = 2;

    private ScrollView weatherLayout;

    private TextView tityCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm15Text;

    private TextView qltText;

    private TextView humText;

    private TextView presText;

    private TextView wdirText;

    private TextView wscText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private String mWeather;

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
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm15Text = (TextView) findViewById(R.id.pm25_text);
        qltText = (TextView) findViewById(R.id.qlt_text);
        humText = (TextView) findViewById(R.id.hum_text);
        presText = (TextView) findViewById(R.id.pres_text);
        wdirText = (TextView) findViewById(R.id.wind_dir_text);
        wscText = (TextView) findViewById(R.id.wind_sc_text);
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

        if (!Objects.isNull(weatherStr) && !Objects.equals("",weatherStr)){
            Weather weather = JSONUtil.handleWeatherRespone(weatherStr);
            mWeatherId = weather.basic.weatherId;
            loadBingPic(weather.now.more.info);

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

    /**
     * 按下返回键
     */
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

    /**
     * 菜单选择项
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //播放音乐
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
        }else if (id == R.id.action_location) { //定位
            requestWeather("CN101190104");
        }else if (id == R.id.action_changeImg){ //修改图片
            chooseFromAlbum();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 权限校验
     */
    private void chooseFromAlbum() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
            openAblum();
        }
    }
    private void openAblum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * 初始化播放器
     * @param mediaPlayer
     */
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

    /**
     * 加载pic
     */
    private void loadBingPic(String weather) {
       setBGPic(weather);
    }

    /**
     * 处理并展示缓存中的数据
     * @param weather
     */
    @SuppressLint("ResourceAsColor")
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
            aqiText.setText("AIQ " + weather.aqi.city.aqi);
            pm15Text.setText("PM2.5 " +weather.aqi.city.pm25);
            qltText.setText(weather.aqi.city.qlty);
            humText.setText("湿度 " + weather.now.hum);
            presText.setText("压强 " + weather.now.pres);
            wdirText.setText("风向 " + weather.now.wind_dir);
            wscText.setText("风力 " + weather.now.wind_sc);
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

    /**
     * 为天气设置图片
     * @param infoText
     * @param info
     */
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
                            loadBingPic(weather.now.more.info);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAblum();
                }else {
                    Toast.makeText(this, "你拒绝了授予权限", Toast.LENGTH_SHORT).show();
                }
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    handleImageOnKitKat(data);
                }
        }
    }

    /**
     * 仅仅支持android4.4以上的版本
     * @param data
     */
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uRi = data.getData();
        if (DocumentsContract.isDocumentUri(this, uRi)){
            //如果是doc的uri类型, 则通过documentid处理
            String docId = DocumentsContract.getDocumentId(uRi);
            if ("com.android.providers.media.documents".equals(uRi.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }else if ("com.android.providers.downloads.documents".equals(uRi.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if ("content".equalsIgnoreCase(uRi.getScheme())){
            imagePath = getImagePath(uRi, null);
        }else if ("file".equalsIgnoreCase(uRi.getScheme())){
            imagePath = uRi.getPath();
        }
        displyImage(imagePath);
    }

    private void displyImage(String imagePath) {
        Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
        Glide.with(MainActivity.this).load(imagePath).into(bingPicImg);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;

        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
    if (cursor != null){
        if (cursor.moveToFirst()){
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
        cursor.close();
    }
    return path;
    }

    /**
     * 设置背景图片
     */
    public void setBGPic(String info){
        int id = R.drawable.bg_sun;
        if (Objects.equals("晴",info)){
            id = R.drawable.bg_sun;
        }else if (Objects.equals("多云",info)){
            id = R.drawable.bg_cloud;
        }else if (Objects.equals("阴",info)){
            id = R.drawable.bg_yin;
        }else if (info.contains("雨")){
            id = R.drawable.bg_rain;
        }else if (info.contains("雪")){
            id = R.drawable.bg_snow;
        }else if (Objects.equals("雾",info)){
            id = R.drawable.bg_fork;
        }

        Glide.with(MainActivity.this).load(id).into(bingPicImg);
    }

    public String setPath(int id){
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + getResources().getResourcePackageName(id) + "/"
                + getResources().getResourceTypeName(id) + "/"
                + getResources().getResourceEntryName(id);
    }

}
