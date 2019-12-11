package com.soundscribe.controller;

import com.soundscribe.dlibra.DLibraService;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dlibra")
public class DLibraController {

  private final DLibraService dLibraService;
  private final SoundscribeConfiguration soundscribeConfiguration;

  @GetMapping("upload")
  public ResponseEntity<String> analyzeFile() {
    File file = new File(soundscribeConfiguration.getSongDataStorage() + "test1.musicxml");
    int id = 0;
    try {
      id = dLibraService.uploadCollection(file, null,1286);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return new ResponseEntity<>("OK" + id, HttpStatus.OK);
  }
}
