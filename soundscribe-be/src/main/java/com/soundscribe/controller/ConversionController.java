package com.soundscribe.controller;

import com.soundscribe.converters.ConverterService;
import com.soundscribe.converters.musicxml.functions.MusicXmlToMidi;
import com.soundscribe.jvamp.JvampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/convert")
public class ConversionController {
  private final JvampService jvampService;
  private final ConverterService converterService;

  @GetMapping("analyze-file")
  public ResponseEntity analyzeFile(@RequestParam String filename) {
    jvampService.loadLibraries();
    File file = new File(filename);
    File wavFile = converterService.convertMP3toWAV(file, false);
    jvampService.pyinNotes(wavFile, false);
    return new ResponseEntity("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-midi")
  public ResponseEntity xmlToMidi(@RequestParam String filename) {
    jvampService.loadLibraries();
    File fileXML = new File(filename);
    converterService.convertXmltoMidi(fileXML, false);
    return new ResponseEntity("Plik midi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-musicxml")
  public ResponseEntity xmlToMusicXml(@RequestParam String filename) {
    jvampService.loadLibraries();
    File fileXML = new File(filename);
    converterService.convertXmltoMusicXml(fileXML, false);
    return new ResponseEntity("Plik musicxml został utworzony", HttpStatus.OK);
  }

  @GetMapping("/musicxml-to-midi")
  public ResponseEntity musicXmlToMidi(@RequestParam String filename) {
    jvampService.loadLibraries();
    File musicXml = new File(filename);
    converterService.convertMusicXmltoMidi(musicXml, false);
    return new ResponseEntity("Plik musicXml został przekonwertowany do midi", HttpStatus.OK);
  }
}
