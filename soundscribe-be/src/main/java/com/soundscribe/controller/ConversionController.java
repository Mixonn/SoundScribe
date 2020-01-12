package com.soundscribe.controller;

import com.soundscribe.converters.ConversionFormat;
import com.soundscribe.converters.Converter;
import com.soundscribe.converters.ConverterService;
import com.soundscribe.converters.xml.FrontXmlPojo;
import com.soundscribe.converters.xml.XmlPojo;
import com.soundscribe.converters.xml.functions.XmlToMusicXml;
import com.soundscribe.jvamp.JvampService;
import com.soundscribe.storage.StorageService;
import com.soundscribe.utilities.CommonUtil;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/convert")
public class ConversionController {

  private final JvampService jvampService;
  private final ConverterService converterService;
  private final SoundscribeConfiguration soundscribeConfiguration;
  private final StorageService storageService;
  private final XmlToMusicXml xmlToMusicXml;

  @PostConstruct
  public void init() {
    jvampService.loadLibraries();
  }

  @GetMapping("analyze-file")
  public ResponseEntity<String> analyzeFile(@RequestParam String filename) {
    try {
      String extension = CommonUtil.getFileExtension(new File(filename));
      File wav;
      if (extension.equals("wav")) {
        wav = new File(soundscribeConfiguration.getSongDataStorage(), filename);
      } else {
        File mp3 = new File(soundscribeConfiguration.getSongDataStorage() + filename);
        wav = converterService.convert(mp3, new ConversionFormat("mp3", "wav"));
      }
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

  /**
   * Updates modified transcription. It supports ABC input formats only. Updated formats: MusicXML,
   * ABC, MIDI, MEI
   */
  @RequestMapping(value = "/update-file-abc", method = RequestMethod.POST)
  public ResponseEntity<String> updateAbc(@RequestParam("file") MultipartFile file) {
    storageService.store(file);
    try {
      File input =
          new File(soundscribeConfiguration.getSongDataStorage(), file.getOriginalFilename());
      File musicXml = converterService.convert(input, new ConversionFormat("abc", "musicxml"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "midi"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "mei"));
    } catch (Exception e) {
      log.error("Update file exception", e);
      return new ResponseEntity<>(
          "Wystąpił błąd podczas przetwarzania utworu", HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>("Plik z danymi wyściowymi został zaaktualizowany", HttpStatus.OK);
  }

  // todo Delete update-file-abc or update-file-abc-raw

  /**
   * Updates modified transcription. It supports ABC input formats only. Updated formats: MusicXML,
   * ABC, MIDI, MEI
   */
  @RequestMapping(value = "/update-file-abc-raw", method = RequestMethod.POST)
  public ResponseEntity<String> updateAbcRaw(
      @RequestParam String fileName, HttpEntity<String> httpEntity) {
    String abcString = httpEntity.getBody();
    if (abcString == null) {
      return new ResponseEntity<>(
          "Wystąpił błąd podczas przetwarzania utworu", HttpStatus.EXPECTATION_FAILED);
    }

    File input = new File(soundscribeConfiguration.getSongDataStorage(), fileName);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(input))) {
      writer.write(abcString);
    } catch (IOException e) {
      return new ResponseEntity<>(
          "Wystąpił błąd podczas przetwarzania utworu", HttpStatus.EXPECTATION_FAILED);
    }

    try {
      File musicXml = converterService.convert(input, new ConversionFormat("abc", "musicxml"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "midi"));
      converterService.convert(musicXml, new ConversionFormat("musicxml", "mei"));
    } catch (Exception e) {
      log.error("Update file exception", e);
      return new ResponseEntity<>(
          "Wystąpił błąd podczas przetwarzania utworu", HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>("Plik z danymi wyściowymi został zaaktualizowany", HttpStatus.OK);
  }

  @RequestMapping(value = "/update-file-midi", method = RequestMethod.POST)
  public ResponseEntity<String> updateMidi(@RequestBody FrontXmlPojo frontXmlPojo) {
    XmlPojo xmlPojo = FrontXmlPojo.convertFrontXmlPojoToXmlPojo(frontXmlPojo);
    try {
      XmlPojo.saveXMLData(xmlPojo, soundscribeConfiguration.getSongDataStorage());
      File musicXml = xmlToMusicXml.convertXmlToMusicXml(xmlPojo);
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
}
