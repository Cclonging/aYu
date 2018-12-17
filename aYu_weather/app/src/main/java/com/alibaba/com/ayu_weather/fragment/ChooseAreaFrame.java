package com.alibaba.com.ayu_weather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.com.ayu_weather.R;
import com.alibaba.com.ayu_weather.WeatherActivity;
import com.alibaba.com.ayu_weather.db.City;
import com.alibaba.com.ayu_weather.db.County;
import com.alibaba.com.ayu_weather.db.Province;
import com.alibaba.com.ayu_weather.util.HttpUtils;
import com.alibaba.com.ayu_weather.util.JSONUtil;

import org.litepal.LitePal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 遍历省市区的随便
 */
public class ChooseAreaFrame extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView title;
    private Button back;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //选择容器
    private List<Province> provinces;
    private List<City> cities;
    private List<County> counties;

    //选择器
    private Province province;
    private City city;
    private County county;

    //当前选中级别
    private int currentLevel;

    //api接口
    private static final String API_URL = "http://guolin.tech/api/china/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.choose_area, container, false);
        title = (TextView) view.findViewById(R.id.title_text);
        back = (Button) view.findViewById(R.id.action_back);
        listView = (ListView) view.findViewById(R.id.list_items);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        province = provinces.get(position);
                        queryCitys(province);
                        break;
                    case LEVEL_CITY:
                        city = cities.get(position);
                        queryCounties(city);
                        break;
                    case LEVEL_COUNTY:
                        county = counties.get(position);
                        String weatherId = county.getWeatherId();
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentLevel){
                    case LEVEL_CITY:
                        queryProvinces();
                        break;
                    case LEVEL_COUNTY:
                        queryCitys(province);
                        break;
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        title.setText("China/中国");
        back.setVisibility(View.GONE);
        provinces = LitePal.findAll(Province.class);
        if (provinces.size() > 0){
            dataList.clear();
            for (Province province : provinces){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(API_URL, "province");
        }
    }

    private void queryCitys(Province province){
        title.setText(province.getProvinceName());
        back.setVisibility(View.VISIBLE);
        cities = LitePal.where("provinceId = ?", String.valueOf(province.getProvinceCode())).find(City.class);
        if (cities.size() > 0){
            dataList.clear();
            for (City city : cities){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            String url = API_URL + province.getProvinceCode();
            queryFromServer(url, "city");
        }
    }

    private void queryCounties(City cityId){
        title.setText(city.getCityName());
        back.setVisibility(View.VISIBLE);
        counties = LitePal.where("cityId=?", String.valueOf(city.getCityCode())).find(County.class);
        if (counties.size() > 0){
            dataList.clear();
            for (County county : counties){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            String url = API_URL + province.getProvinceCode() + "/" + city.getCityCode();
            queryFromServer(url, "county");
        }
    }

    private void queryFromServer(String url, final String type) {
        showProgressDialog();
        HttpUtils.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgressDialog();
                Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = JSONUtil.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = JSONUtil.handleCityResponse(responseText, province.getProvinceCode());
                }else if ("county".equals(type)){
                    result = JSONUtil.handleCountyResponse(responseText, city.getCityCode());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCitys(province);
                            }else if ("county".equals(type)){
                                queryCounties(city);
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
