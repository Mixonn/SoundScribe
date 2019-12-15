package com.soundscribe.controller;

import com.soundscribe.dlibra.DLibraService;
import com.soundscribe.utilities.SoundscribeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dlibra")
public class DLibraController {

  private final DLibraService dLibraService;
  private final SoundscribeConfiguration soundscribeConfiguration;

  @GetMapping("upload")
  public ResponseEntity<String> upload(
      @RequestParam Integer publicationID,
      @RequestParam String musicXmlName,
      @RequestParam String[] filesNames) {
    File musicXml = new File(soundscribeConfiguration.getSongDataStorage() + musicXmlName);
    ArrayList<File> files = new ArrayList<>();

    for (String fileName : filesNames) {
      Path path = Paths.get(soundscribeConfiguration.getSongDataStorage() + fileName);
      if (Files.exists(path)) {
        files.add(new File(path.toUri()));
      }
    }

    int receivedID;
    try {
      receivedID = dLibraService.uploadCollection(musicXml, files, publicationID);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>("Failed to upload publication.", HttpStatus.EXPECTATION_FAILED);
    }
    return new ResponseEntity<>(
        "Publication was successful uploaded. Publication ID: " + receivedID, HttpStatus.OK);
  }

    @GetMapping("download")
    public ResponseEntity<String> download(@RequestParam Integer publicationID) {
        try {
            dLibraService.downloadCollection(publicationID);
            return new ResponseEntity<>("Publication was successfully downloaded.", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to download publication.", HttpStatus.EXPECTATION_FAILED);
        }
    }
}