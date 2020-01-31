package com.soundscribe.controller;

import com.soundscribe.dlibra.DLibraService;
import com.soundscribe.dlibra.PublicationInfo;
import com.soundscribe.dlibra.XMLService;
import com.soundscribe.utilities.CommonUtil;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dlibra")
public class DLibraController {

  private final DLibraService dLibraService;
  private final SoundscribeConfiguration soundscribeConfiguration;

  @GetMapping("upload")
  @PreAuthorize("hasAuthority('SCOPE_soundscribe-edit')")
  public ResponseEntity<String> upload(@RequestParam String file) {
    String fileNameWithOutExt = file.replaceFirst("[.][^.]+$", "");
    File abcFile = new File(soundscribeConfiguration.getSongDataStorage() + file);
    File musicXml =
        new File(soundscribeConfiguration.getSongDataStorage() + fileNameWithOutExt + ".musicxml");
    File xmlFile =
        new File(soundscribeConfiguration.getSongDataStorage() + fileNameWithOutExt + ".xml");
    File midFile =
        new File(soundscribeConfiguration.getSongDataStorage() + fileNameWithOutExt + ".midi");
    ArrayList<File> files = new ArrayList<>();
    files.add(abcFile);
    files.add(xmlFile);
    files.add(midFile);

    try {
      Path publicationInfoPath =
          Paths.get(
              soundscribeConfiguration.getSongDataStorage()
                  + CommonUtil.getFileNameWithoutExtension(musicXml)
                  + ".publicationid");

      if (Files.exists(publicationInfoPath)) {
        Scanner scanner = new Scanner(publicationInfoPath);
        String publicationTitle = scanner.nextLine();
        Integer publicationId = Integer.parseInt(scanner.nextLine());
        dLibraService.uploadCollection(musicXml, files, publicationId, publicationTitle);
        scanner.close();
      } else {
        int receivedID = dLibraService.uploadCollection(musicXml, files, null, fileNameWithOutExt);
        PrintWriter printWriter = new PrintWriter(new File(publicationInfoPath.toUri()));
        printWriter.println(fileNameWithOutExt);
        printWriter.println(receivedID);
        printWriter.close();
      }

    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
    }

    return new ResponseEntity<>("Publication was successful uploaded.", HttpStatus.OK);
  }

  @GetMapping("download")
  @PreAuthorize("hasAuthority('SCOPE_soundscribe-read')")
  public ResponseEntity<String> download(@RequestParam String publicationToDownload) {
    try {
      Integer publicationID = Integer.parseInt(publicationToDownload.split(":")[1]);
      dLibraService.downloadCollection(publicationID);
      return new ResponseEntity<>("Publication was successfully downloaded.", HttpStatus.OK);
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to download publication.", HttpStatus.EXPECTATION_FAILED);
    }
  }

  @GetMapping("/list-files")
  @PreAuthorize("hasAuthority('SCOPE_soundscribe-read')")
  public List<String> listFiles() {
    List<PublicationInfo> publications = XMLService.parseListResponse();
    return publications.stream()
        .map(p -> p.getTitle() + ":" + p.getId())
        .collect(Collectors.toList());
  }
}
