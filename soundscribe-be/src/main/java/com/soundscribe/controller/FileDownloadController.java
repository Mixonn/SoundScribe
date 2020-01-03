package com.soundscribe.controller;

import com.soundscribe.storage.StorageService;
import com.soundscribe.utilities.SoundscribeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/download")
@Slf4j
public class FileDownloadController {
  private final SoundscribeConfiguration soundscribeConfiguration;
  private final StorageService storageService;

  @GetMapping("/list-files")
  public List<String> listFiles(String extension) {
    return storageService
            .loadAll()
            .map(x -> x.getFileName().toString())
            .filter(x -> x.endsWith(extension))
            .collect(Collectors.toList());
  }

  @RequestMapping("/{filename}")
  public void downloadFile(
          HttpServletRequest request,
          HttpServletResponse response,
          @PathVariable("filename") String filename) {
    Path file = Paths.get(soundscribeConfiguration.getSongDataStorage(), filename);
    if (Files.exists(file)) {
      response.setContentType("application/pdf");
      response.addHeader("Content-Disposition", "attachment; filename=" + filename);
      try {
        Files.copy(file, response.getOutputStream());
        response.getOutputStream().flush();
      } catch (IOException ex) {
        log.error("Error while downloading files!", ex);
      }
    }
  }
}
