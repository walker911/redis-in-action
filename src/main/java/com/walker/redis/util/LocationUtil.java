package com.walker.redis.util;

import java.util.Arrays;

/**
 * @author walker
 * @date 2019/10/17
 */
public class LocationUtil {

    private LocationUtil() {

    }

    /**
     * 赤道半径
     */
    private static final double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 通过经纬度获取距离(单位：米)
     *
     * @param lat1 纬度
     * @param lng1 经度
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }

    public static void main(String[] args) {
        double lng1 = 113.585896;
        double lat1 = 23.133549;
        double lng2 = 113.31513;
        double lat2 = 23.399405;
        System.out.println(getDistance(lat1, lng1, lat2, lng2));
        Double[] location1 = {113.273336, 23.196527};
        Double[] location2 = {113.273336, 23.196527};
        System.out.println(Arrays.equals(location1, location2));

    }
}
