package com.example.hefengtest;

/**
 * Created by 王将 on 2018/4/24.
 */

public class Provide {

    private static String sunUp="";
    private static String sunDown="";
    private static boolean isUp=false;
    private static boolean isDown=false;

    public static void setIsDown(boolean isDown) {
        Provide.isDown = isDown;
    }

    public static void setIsUp(boolean isUp) {
        Provide.isUp = isUp;
    }

    public static boolean isIsDown() {
        return isDown;
    }

    public static boolean isIsUp() {
        return isUp;
    }

    public static void setSunDown(String sunDown) {
        Provide.sunDown = sunDown;
    }

    public static void setSunUp(String sunUp) {
        Provide.sunUp = sunUp;
    }

    public static String getSunDown() {
        return sunDown;
    }

    public static String getSunUp() {
        return sunUp;
    }

}
