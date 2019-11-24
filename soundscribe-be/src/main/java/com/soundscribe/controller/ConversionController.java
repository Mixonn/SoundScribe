package com.soundscribe.controller;

import com.soundscribe.converters.ConversionFormat;
import com.soundscribe.converters.ConverterService;
import com.soundscribe.jvamp.JvampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ConversionController {
  private final JvampService jvampService;
  private final ConverterService converterService;

  @GetMapping("analyze-file")
  public ResponseEntity<String> analyzeFile(@RequestParam String filename) {
    File file = new File(filename);
    File wavFile = converterService.convert(file, new ConversionFormat("mp3", "wav")).get();
    jvampService.pyinNotes(wavFile, false);
    return new ResponseEntity<>("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @GetMapping("convert")
  public ResponseEntity<String> convert(
          @RequestParam String from, @RequestParam String to, @RequestParam String filename) {
    File input = new File(filename);
    Optional<File> converted = converterService.convert(input, new ConversionFormat(from, to));

    String responsePrefix = "Konwersja z " + from + " do " + to + " ";
    if (converted.isPresent()) {
      return new ResponseEntity<>(responsePrefix + "udana!", HttpStatus.OK);
    } else {
      return new ResponseEntity<>(responsePrefix + "nieudana!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
