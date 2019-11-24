package com.soundscribe.jvamp;

import org.vamp_plugins.RealTime;

/**
 * Static helper class which converts RealTime type from jVamp to other types.
 */
public class TimeHelper {
  public static double realTime2Double(RealTime time) {
    return Double.parseDouble(time.toString());
  }

  public static String realTime2String(RealTime time, int precision) {
    return String.format("%." + precision + "f", realTime2Double(time)).replace(",", ".");
  }
}
