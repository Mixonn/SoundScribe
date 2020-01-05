package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;

class MeiConverter extends Converter {
  MeiConverter(SoundscribeConfiguration soundscribeConfiguration) {
    super(soundscribeConfiguration);
  }

  @Override
  public File convert(File input, String outputFormat) throws ConversionNotSupported {
    CrossPlatformConverter platformConverter = new CrossPlatformConverter(input);
    if ("musicxml".equals(outputFormat)) {
      return platformConverter.convertMeiToMusicXml();
    }

    throw new ConversionNotSupported(getName() + " to " + outputFormat);
  }

  @Override
  public String getName() {
    return "mei";
  }
}
