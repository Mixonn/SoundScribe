package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import javazoom.jl.decoder.JavaLayerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Mp3Converter extends Converter {
  Mp3Converter(SoundscribeConfiguration soundscribeConfiguration) {
    super(soundscribeConfiguration);
  }

  @Override
  public File convert(File input, String outputFormat) throws ConversionNotSupported {
    if ("wav".equals(outputFormat)) {
      return convertMP3toWAV(input);
    }

    throw new ConversionNotSupported(getName() + " to " + outputFormat);
  }

  @Override
  String getName() {
    return "mp3";
  }

  private File convertMP3toWAV(File fileMp3) {
    String fileName = fileMp3.getName().split("\\.")[0];
    File fileWav = new File(soundscribeConfiguration.getSongDataStorage() + fileName + ".wav");
    javazoom.jl.converter.Converter converter = new javazoom.jl.converter.Converter();

    try {
      converter.convert(fileMp3.getAbsolutePath(), fileWav.getAbsolutePath());
    } catch (JavaLayerException e) {
      log.error("An error occurred while converting mp3 to wav", e);
      throw new RuntimeException(e);
    }
    return fileWav;
  }
}
