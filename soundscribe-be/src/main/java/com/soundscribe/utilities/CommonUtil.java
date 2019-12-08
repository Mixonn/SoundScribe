package com.soundscribe.utilities;

import java.io.File;

public class CommonUtil {
public static String getFileNameWithoutExtension(File file){
  return file.getName().split("\\.")[0];
}
}
