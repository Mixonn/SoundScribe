package com.soundscribe.controller;

import com.soundscribe.converters.ConversionFormat;
import com.soundscribe.converters.Converter;
import com.soundscribe.converters.ConverterService;
import com.soundscribe.jvamp.JvampService;
import com.soundscribe.utilities.CommonUtil;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/convert")
public class ConversionController {

  private final JvampService jvampService;
  private final ConverterService converterService;
  private final SoundscribeConfiguration soundscribeConfiguration;

  @PostConstruct
  public void init() {
    jvampService.loadLibraries();
  }

  @GetMapping("analyze-file")
  public ResponseEntity<String> analyzeFile(@RequestParam String filename) {
    try {
      File mp3 = new File(soundscribeConfiguration.getSongDataStorage() + filename);
      File wav = converterService.convert(mp3, new ConversionFormat("mp3", "wav"));
      File xml = jvampService.pyinNotes(wav, false);
      jvampService.pyinSmoothedPitchTrack(wav, false);
      File musicXml = converterService.convert(xml, new ConversionFormat("xml", "musicxml"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "midi"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "mei"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "abc"));
    } catch (Exception e) {
      log.error("Analyze file exception", e);
      return new ResponseEntity<>(
          "Wystąpił błąd podczas przetwarzania utworu", HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>("Plik z danymi wyściowymi został utworzony", HttpStatus.OK);
  }

  @GetMapping("update-file")
  /**
   * This endpoint updates modified transcription. It supports ABC and MIDI input formats only.
   * Updated formats: MusicXML, ABC, MIDI, MEI
   */
  public ResponseEntity<String> updateFile(@RequestParam String filename) {
    try {
      File input = new File(soundscribeConfiguration.getSongDataStorage() + filename);
      String extension = CommonUtil.getFileExtension(input);
      File musicXml = converterService.convert(input, new ConversionFormat(extension, "musicxml"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "midi"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "mei"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "abc"));
    } catch (Exception e) {
      log.error("Update file exception", e);
      return new ResponseEntity<>(
          "Wystąpił błąd podczas przetwarzania utworu", HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>("Plik z danymi wyściowymi został zaaktualizowany", HttpStatus.OK);
  }

  @GetMapping("")
  public ResponseEntity<String> convert(
      @RequestParam String from, @RequestParam String to, @RequestParam String filename) {
    File input = new File(soundscribeConfiguration.getSongDataStorage() + filename);
    String responsePrefix = "Konwersja z " + from + " do " + to + " ";

    try {
      converterService.convert(input, new ConversionFormat(from, to));
      return new ResponseEntity<>(responsePrefix + "udana!", HttpStatus.OK);
    } catch (Converter.ConversionNotSupported exception) {
      log.debug(exception.getMessage());
      return new ResponseEntity<>("Konwersja niewspierana!", HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception exception) {
      log.debug(exception.getMessage());
      return new ResponseEntity<>(responsePrefix + "nieudana!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
