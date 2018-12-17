package com.alibaba.com.ayu_weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 截取HeWeather 的JSON返回数据的格式
 * [
 *      {
 *          "status": ok,
 *          "basic": {},
 *          "api":  {},
 *          "now": {},
 *          "suggestion": {},
 *          "daily_forecast": {}
 *      }
 * ]
 */
public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;

    }
}
