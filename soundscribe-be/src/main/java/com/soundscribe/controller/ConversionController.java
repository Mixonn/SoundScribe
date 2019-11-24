package com.soundscribe.controller;

import com.soundscribe.converters.ConverterService;
import com.soundscribe.jvamp.JvampService;
import com.soundscribe.utilities.SoundscribeConfiguration;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/convert")
public class ConversionController {

  private final JvampService jvampService;
  private final ConverterService converterService;
  private final SoundscribeConfiguration soundscribeConfiguration;

  @GetMapping("analyze-file")
  public ResponseEntity<String> analyzeFile(@RequestParam String filename) {
    try {
      File file = new File(soundscribeConfiguration.getSongDataStorage() + filename);
      File wavFile = converterService.convertMP3toWAV(file, false);
      File xmlFile = jvampService.pyinNotes(wavFile, false);
      File musicxmlFile = converterService.convertXmltoMusicXml(xmlFile, false);
      converterService.convertMusicXmltoMidi(musicxmlFile, false);
    } catch (Exception e) {
      log.error("Analyze file exception", e);
      return new ResponseEntity<>("Wystąpił błąd podczas przetwarzania utworu",
          HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @PostConstruct
  public void init() {
    jvampService.loadLibraries();
  }
}
