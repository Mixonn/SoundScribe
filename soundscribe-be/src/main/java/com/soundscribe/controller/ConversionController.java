package com.soundscribe.controller;

import com.soundscribe.converters.ConverterService;
import com.soundscribe.jvamp.JvampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/convert")
public class ConversionController {
  private final JvampService jvampService;
  private final ConverterService converterService;

  @GetMapping("analyze-file")
  public ResponseEntity<String> analyzeFile(@RequestParam String filename) {
    File fileMp3 = new File(filename);
    File wavFile = converterService.convertMP3toWAV(fileMp3, false);
    jvampService.pyinNotes(wavFile, fileMp3, false);
    return new ResponseEntity<>("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-midi")
  public ResponseEntity<String> xmlToMidi(@RequestParam String filename) {
    File fileXML = new File(filename);
    converterService.convertXmltoMidi(fileXML, false);
    return new ResponseEntity<>("Plik midi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-musicxml")
  public ResponseEntity<String> xmlToMusicXml(@RequestParam String filename) {
    File fileXML = new File(filename);
    converterService.convertXmltoMusicXml(fileXML, false);
    return new ResponseEntity<>("Plik musicxml został utworzony", HttpStatus.OK);
  }

  @GetMapping("/musicxml-to-midi")
  public ResponseEntity<String> musicXmlToMidi(@RequestParam String filename) {
    File musicXml = new File(filename);
    converterService.convertMusicXmltoMidi(musicXml, false);
    return new ResponseEntity<>("Plik musicXml został przekonwertowany do midi", HttpStatus.OK);
  }

  @PostConstruct
  public void init() {
    jvampService.loadLibraries();
  }
}
