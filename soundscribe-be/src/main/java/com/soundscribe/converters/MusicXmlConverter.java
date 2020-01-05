package com.soundscribe.converters;

import com.soundscribe.converters.musicxml.functions.MusicXmlToMidi;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MusicXmlConverter extends Converter {

  private SoundscribeConfiguration soundscribeConfiguration;
  private final MusicXmlToMidi musicXmlToMidi;

  MusicXmlConverter(SoundscribeConfiguration soundscribeConfiguration) {
    super(soundscribeConfiguration);
    musicXmlToMidi = new MusicXmlToMidi(new XmlConverter(soundscribeConfiguration));
  }

  @Override
  File convert(File input, String outputFormat) throws ConversionNotSupported {
    if ("midi".equals(outputFormat)) {
      return musicXmlToMidi.convertMusicXmlToMidi(input);
    } else if ("mei".equals(outputFormat)) {
      return new CrossPlatformConverter(input).convertMusicXmlToMei();
    } else if ("abc".equals(outputFormat)) {
      return new CrossPlatformConverter(input).convertMusicXmlToAbc();
    }

    throw new ConversionNotSupported(getName() + " to " + outputFormat);
  }

  @Override
  String getName() {
    return "musicxml";
  }
}
