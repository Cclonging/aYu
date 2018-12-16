package com.alibaba.com.ayu_weather.util;

import com.alibaba.com.ayu_weather.db.City;
import com.alibaba.com.ayu_weather.db.County;
import com.alibaba.com.ayu_weather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 处理JSON格式数据的 工具类
 * @author aYu
 * @date 2018-12-16
 */
public class JSONUtil {

    /**
     * 解析和处理服务器返回省和直辖市的数据 并保存到数据库
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response){
        if (!Utility.isEmpty(response)){
            try {
                JSONArray allprovinces = new JSONArray(response);
                for (int i = 0; i < allprovinces.length(); i ++){
                    JSONObject provinceObj = allprovinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObj.getString("name"));
                    province.setProvinceCode(provinceObj.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回市的数据 并保存到数据库
     * @param response
     * @param province
     * @return
     */
    public static boolean handleCityResponse(String response, int province){
        if (!Utility.isEmpty(response)){
            try {
                JSONArray allcities = new JSONArray(response);
                for (int i = 0; i < allcities.length(); i ++){
                    JSONObject cityObj = allcities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObj.getString("name"));
                    city.setCityCode(cityObj.getInt("id"));
                    city.setProvinceId(province);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回市的数据 并保存到数据库
     * @param response
     * @param city
     * @return
     */
    public static boolean handleCountyResponse(String response, int city){
        if (!Utility.isEmpty(response)){
            try {
                JSONArray allcounties = new JSONArray(response);
                for (int i = 0; i < allcounties.length(); i ++){
                    JSONObject countyObj = allcounties.getJSONObject(i);
                    County county = new County();
                    county.setCityId(city);
                    county.setCountyName(countyObj.getString("name"));
                    county.setWeatherId(countyObj.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
