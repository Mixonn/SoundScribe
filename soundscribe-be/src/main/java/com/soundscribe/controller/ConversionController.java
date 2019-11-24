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
      File fileMp3 = new File(soundscribeConfiguration.getSongDataStorage() + filename);
      File wavFile = converterService.convertMP3toWAV(fileMp3, false);
      File xmlFile = jvampService.pyinNotes(wavFile,fileMp3, false);
      File musicxmlFile = converterService.convertXmltoMusicXml(xmlFile, false);
      converterService.convertMusicXmltoMidi(musicxmlFile, false);
    } catch (Exception e) {
      log.error("Analyze file exception", e);
      return new ResponseEntity<>("Wystąpił błąd podczas przetwarzania utworu",
          HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-midi")
  public ResponseEntity<String> xmlToMidi(@RequestParam String filename) {
    File fileXML = new File(soundscribeConfiguration.getSongDataStorage() + filename);
    converterService.convertXmltoMidi(fileXML, false);
    return new ResponseEntity<>("Plik midi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-musicxml")
  public ResponseEntity<String> xmlToMusicXml(@RequestParam String filename) {
    File fileXML = new File(soundscribeConfiguration.getSongDataStorage() + filename);
    converterService.convertXmltoMusicXml(fileXML, false);
    return new ResponseEntity<>("Plik musicxml został utworzony", HttpStatus.OK);
  }

  @GetMapping("/musicxml-to-midi")
  public ResponseEntity<String> musicXmlToMidi(@RequestParam String filename) {
    File musicXml = new File(soundscribeConfiguration.getSongDataStorage() + filename);
    converterService.convertMusicXmltoMidi(musicXml, false);
    return new ResponseEntity<>("Plik musicXml został przekonwertowany do midi", HttpStatus.OK);
  }

  @PostConstruct
  public void init() {
    jvampService.loadLibraries();
  }
}
