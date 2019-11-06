package com.soundscribe.converter;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ConverterService {

  @Autowired
  private MidiConverter midiConverter;
  @Autowired
  private SoundscribeConfiguration soundscribeConfiguration;
  private static final Logger logger = LoggerFactory.getLogger(ConverterService.class);

  public File convertMP3toWAV(File fileMp3, boolean deleteAfter) {
    String fileName = fileMp3.getName().split("\\.")[0];
    File fileWav = new File(soundscribeConfiguration.getSongDataStorage() + fileName + ".wav");
    Converter converter = new Converter();

    try {
      converter.convert(fileMp3.getAbsolutePath(), fileWav.getAbsolutePath());
    } catch (JavaLayerException e) {
      logger.error("An error occurred while converting mp3 to wav");
      throw new RuntimeException(e);
    }

    if (deleteAfter) {
      try {
        Files.delete(fileMp3.toPath());
      } catch (IOException e) {
        logger.debug("The MP3 file cannot be deleted. This file no longer exists.");
      }
    }
    return fileWav;
  }

  public File convertXMLtoMIDI(File fileXML, boolean deleteAfter) {
    File midi = midiConverter.convertXmlToMidi(fileXML);
    if (deleteAfter) {
      try {
        Files.delete(fileXML.toPath());
      } catch (IOException e) {
        logger.debug("The XML file cannot be deleted. This file no longer exists.");
      }
    }
    return midi;
  }
}
