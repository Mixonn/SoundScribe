package com.soundscribe;

import com.soundscribe.converters.ConverterService;
import com.soundscribe.jvamp.JvampService;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
//todo Delete this class
public class OnlyForShow {
  private final JvampService jvampService;
  private final ConverterService converterService;
  private final SoundscribeConfiguration soundscribeConfiguration;

  public void analyzeFile(String filename) {
    try {
      File file = new File(soundscribeConfiguration.getSongDataStorage() + filename);
      File wavFile = converterService.convertMP3toWAV(file, false);
      File xmlFile = jvampService.pyinNotes(wavFile, false);
      File musicxmlFile = converterService.convertXmltoMusicXml(xmlFile, false);
      converterService.convertMusicXmltoMidi(musicxmlFile, false);
    } catch (Exception e) {
      log.error("Analyze file exception", e);
    }
  }
}

