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
    File file = new File(filename);
    File wavFile = converterService.convertMP3toWAV(file, false);
    jvampService.pyinNotes(wavFile, false);
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

  @GetMapping("/musicxml-to-mei")
  public ResponseEntity musicXmlToMei(@RequestParam String filename) {
    File file = converterService.convertMusicXmlToMei(filename);
    if (file != null) {
      return new ResponseEntity("Plik MusicXML został przekonwertowany do MEI", HttpStatus.OK);
    } else {
      return new ResponseEntity("Konwersja nieudana", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/mei-to-musicxml")
  public ResponseEntity meiToMusicXml(@RequestParam String filename) {
    File file = converterService.convertMeiToMusicXml(filename);
    if (file != null) {
      return new ResponseEntity("Plik MEI został przekonwertowany do MusicXML", HttpStatus.OK);
    } else {
      return new ResponseEntity("Konwersja nieudana", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/musicxml-to-abc")
  public ResponseEntity musicXmlToAbc(@RequestParam String filename) {
    File file = converterService.convertMusicXmlToAbc(filename);
    if (file != null) {
      return new ResponseEntity("Plik MusicXML został przekonwertowany do ABC", HttpStatus.OK);
    } else {
      return new ResponseEntity("Konwersja nieudana", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/abc-to-musicxml")
  public ResponseEntity abcToMusicXml(@RequestParam String filename) {
    File file = converterService.convertAbcToMusicXml(filename);
    if (file != null) {
      return new ResponseEntity("Plik ABC został przekonwertowany do MusicXML", HttpStatus.OK);
    } else {
      return new ResponseEntity("Konwersja nieudana", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
