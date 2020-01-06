package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConverterService {

  private final SoundscribeConfiguration soundscribeConfiguration;
  private final MidiConverter midiConverter;

  public File convert(File input, ConversionFormat conversionFormat)
      throws Converter.ConversionNotSupported {
    Converter converter = null;
    if ("abc".equals(conversionFormat.getFromFormat())) {
      converter = new AbcConverter(soundscribeConfiguration);
    } else if ("mei".equals(conversionFormat.getFromFormat())) {
      converter = new MeiConverter(soundscribeConfiguration);
    } else if ("xml".equals(conversionFormat.getFromFormat())) {
      converter = new XmlConverter(soundscribeConfiguration);
    } else if ("musicxml".equals(conversionFormat.getFromFormat())) {
      converter = new MusicXmlConverter(soundscribeConfiguration);
    } else if ("mp3".equals(conversionFormat.getFromFormat())) {
      converter = new Mp3Converter(soundscribeConfiguration);
    } else if ("midi".equals(conversionFormat.getFromFormat())) {
      converter = midiConverter;
    } else {
      throw new Converter.ConversionNotSupported(conversionFormat.toString());
    }

    return converter.convert(input, conversionFormat.getToFormat());
  }
}
