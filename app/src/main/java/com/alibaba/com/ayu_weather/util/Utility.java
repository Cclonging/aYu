package com.alibaba.com.ayu_weather.util;

/**
 * 一般操作的工具库
 * @author aYu
 * @date 2018-12-16
 */
public class Utility {

    public static boolean isEmpty(String str){
        if (str == null && str.length() == 0 && "".equals(str))
            return true;
        return false;
    }
}
