package com.soundscribe.jvamp;

import org.vamp_plugins.RealTime;

/**
 * Static helper class which converts RealTime type from jVamp to other types.
 */
public class TimeHelper {
    public static double RealTime2Double(RealTime time){
        return Double.parseDouble(time.toString());
    }

    public static String RealTime2String(RealTime time, int precision){
        return String.format("%." + precision + "f", RealTime2Double(time)).replace(",", ".");
    }
}
