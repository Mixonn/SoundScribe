package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class Converter {
  protected SoundscribeConfiguration soundscribeConfiguration;

  abstract File convert(File input, String outputFormat) throws ConversionNotSupported;

  abstract String getName();

  public static class ConversionNotSupported extends Exception {
    public ConversionNotSupported(String message) {
      super(message);
    }
  }
}
