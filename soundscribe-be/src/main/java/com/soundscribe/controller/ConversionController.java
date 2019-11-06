package com.soundscribe.controller;

import com.soundscribe.converter.ConverterService;
import com.soundscribe.jvamp.JvampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
@RequiredArgsConstructor
@RequestMapping("/convert")
public class ConversionController {
  private final JvampService jvampService;
  private final ConverterService converterService;

  @GetMapping("analyze-file")
  @ResponseBody
  public ResponseEntity analyzeFile(@RequestParam String filename) {
    jvampService.loadLibraries();
    File file = new File(filename);
    File wavFile = converterService.convertMP3toWAV(file, false);
    jvampService.pyinNotes(wavFile, false);
    return new ResponseEntity("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @GetMapping("/xml-to-midi")
  @ResponseBody
  public ResponseEntity xmlToMidi(@RequestParam String filename) {
    jvampService.loadLibraries();
    File fileXML = new File(filename);
    converterService.convertXMLtoMIDI(fileXML, false);
    return new ResponseEntity("Plik midi został utworzony", HttpStatus.OK);
  }
}
