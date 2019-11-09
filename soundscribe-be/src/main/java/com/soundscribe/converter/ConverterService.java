package com.soundscribe.converter;

import com.soundscribe.converter.musicxml.MusicXmlConverter;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConverterService {

  private final MidiConverter midiConverter;
  private final MusicXmlConverter musicXmlConverter;
  private final SoundscribeConfiguration soundscribeConfiguration;

  public File convertMP3toWAV(File fileMp3, boolean deleteAfter) {
    String fileName = fileMp3.getName().split("\\.")[0];
    File fileWav = new File(soundscribeConfiguration.getSongDataStorage() + fileName + ".wav");
    Converter converter = new Converter();

    try {
      converter.convert(fileMp3.getAbsolutePath(), fileWav.getAbsolutePath());
    } catch (JavaLayerException e) {
      log.error("An error occurred while converting mp3 to wav", e);
      throw new RuntimeException(e);
    }

    if (deleteAfter) {
      try {
        Files.delete(fileMp3.toPath());
      } catch (IOException e) {
        log.debug("The MP3 file cannot be deleted. This file no longer exists.", e);
      }
    }
    return fileWav;
  }

  public File convertXmltoMidi(File fileXML, boolean deleteAfter) {
    File midi = midiConverter.convertXmlToMidi(fileXML);
    if (deleteAfter) {
      try {
        Files.delete(fileXML.toPath());
      } catch (IOException e) {
        log.debug("The XML file cannot be deleted. This file no longer exists.", e);
      }
    }
    return midi;
  }

  public File convertXmltoMusicXml(File fileXML, boolean deleteAfter) {
    File musicXml = null;
    try {
      musicXml = musicXmlConverter.convertXmlToMidi(fileXML);
    } catch (ParserConfigurationException | TransformerException e) {
      e.printStackTrace();
    }
    if (deleteAfter) {
      try {
        Files.delete(fileXML.toPath());
      } catch (IOException e) {
        log.debug("The XML file cannot be deleted. This file no longer exists.", e);
      }
    }
    return musicXml;
  }
}
