package com.soundscribe.converters;

import com.soundscribe.converters.musicxml.converter.MusicXmlConverter;
import com.soundscribe.utilities.SoundscribeConfiguration;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConverterService {

  private final MidiConverter midiConverter;
  private final MusicXmlConverter musicXmlConverter;
  private final SoundscribeConfiguration soundscribeConfiguration;

  public Optional<File> convert(File input, ConversionFormat conversionFormat) {
    if (conversionFormat.equals(new ConversionFormat("mp3", "wav"))) {
      return Optional.of(convertMP3toWAV(input));
    } else if (conversionFormat.equals(new ConversionFormat("xml", "midi"))) {
      return Optional.of(convertXmltoMidi(input));
    } else if (conversionFormat.equals(new ConversionFormat("xml", "musicxml"))) {
      return Optional.of(convertXmltoMusicXml(input));
    } else if (conversionFormat.equals(new ConversionFormat("musicxml", "midi"))) {
      return Optional.of(convertMusicXmltoMidi(input));
    } else if (conversionFormat.equals(new ConversionFormat("musicxml", "mei"))) {
      return Optional.ofNullable(convertMusicXmlToMei(input));
    } else if (conversionFormat.equals(new ConversionFormat("mei", "musicxml"))) {
      return Optional.ofNullable(convertMeiToMusicXml(input));
    } else if (conversionFormat.equals(new ConversionFormat("musicxml", "abc"))) {
      return Optional.ofNullable(convertMusicXmlToAbc(input));
    } else if (conversionFormat.equals(new ConversionFormat("abc", "musicxml"))) {
      return Optional.ofNullable(convertAbcToMusicXml(input));
    }
    return Optional.empty();
  }

  private File convertMP3toWAV(File fileMp3) {
    String fileName = fileMp3.getName().split("\\.")[0];
    File fileWav = new File(soundscribeConfiguration.getSongDataStorage() + fileName + ".wav");
    Converter converter = new Converter();

    try {
      converter.convert(fileMp3.getAbsolutePath(), fileWav.getAbsolutePath());
    } catch (JavaLayerException e) {
      log.error("An error occurred while converting mp3 to wav", e);
      throw new RuntimeException(e);
    }
    return fileWav;
  }

  private File convertXmltoMidi(File fileXML) {
    return midiConverter.convertXmlToMidi(fileXML);
  }

  private File convertXmltoMusicXml(File fileXML) {
    return musicXmlConverter.convertXmlToMusicXml(fileXML);
  }

  private File convertMusicXmltoMidi(File musicXml) {
    return musicXmlConverter.convertMusicXmlToMidi(musicXml);
  }

  private File convertMusicXmlToMei(File musicXml) {
    return new CrossPlatformConverter(musicXml).convertMusicXmlToMei();
  }

  private File convertMeiToMusicXml(File mei) {
    return new CrossPlatformConverter(mei).convertMeiToMusicXml();
  }

  private File convertMusicXmlToAbc(File musicXml) {
    return new CrossPlatformConverter(musicXml).convertMusicXmlToAbc();
  }

  private File convertAbcToMusicXml(File abc) {
    return new CrossPlatformConverter(abc).convertAbcToMusicXml();
  }
}
