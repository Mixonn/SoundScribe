package com.soundscribe.jvamp;

import org.vamp_plugins.RealTime;

public class TimeHelper {
    public static double RealTime2Double(RealTime time){
        return Double.parseDouble(time.toString());
    }

    public static String RealTime2String(RealTime time, int precision){
        return String.format("%." + precision + "f", RealTime2Double(time)).replace(",", ".");
    }
}
