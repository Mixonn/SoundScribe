package com.soundscribe.converters;

import com.soundscribe.converters.midi.functions.MidiToMusicXml;
import com.soundscribe.converters.xml.functions.XmlToMusicXml;
import com.soundscribe.utilities.MusicXmlConfiguration;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Converts midi to any other music format */
@Slf4j
@Component
public class MidiConverter extends Converter {

  private final MidiToMusicXml midiToMusicXml;

  public MidiConverter(SoundscribeConfiguration soundscribeConfiguration) {
    super(soundscribeConfiguration);
    midiToMusicXml =
        new MidiToMusicXml(
            new XmlToMusicXml(soundscribeConfiguration, new MusicXmlConfiguration()));
  }

  @Override
  File convert(File input, String outputFormat) throws ConversionNotSupported {
    if ("musicxml".equals(outputFormat)) {
      return midiToMusicXml.convertMidiToMusicXml(input);
    }

    throw new ConversionNotSupported(getName() + " to " + outputFormat);
  }

  @Override
  String getName() {
    return "midi";
  }
}
