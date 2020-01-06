package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;

class AbcConverter extends Converter {
  AbcConverter(SoundscribeConfiguration soundscribeConfiguration) {
    super(soundscribeConfiguration);
  }

  @Override
  public File convert(File input, String outputFormat) throws ConversionNotSupported {
    CrossPlatformConverter platformConverter = new CrossPlatformConverter(input);
    if ("musicxml".equals(outputFormat)) {
      return platformConverter.convertAbcToMusicXml();
    }

    throw new ConversionNotSupported(getName() + " to " + outputFormat);
  }

  @Override
  String getName() {
    return "abc";
  }
}
