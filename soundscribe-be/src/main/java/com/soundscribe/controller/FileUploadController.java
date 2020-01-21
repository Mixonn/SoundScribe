package com.soundscribe.controller;

import com.soundscribe.storage.StorageFileNotFoundException;
import com.soundscribe.storage.StorageService;
import com.soundscribe.utilities.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class FileUploadController {

  private final StorageService storageService;
  private final ConversionController conversionController;

  @GetMapping("/")
  public String mainPage(Model model) throws IOException {

    model.addAttribute(
        "files",
        storageService
            .loadAll()
            .map(
                path ->
                    MvcUriComponentsBuilder.fromMethodName(
                            FileUploadController.class, "serveFile", path.getFileName().toString())
                        .build()
                        .toString())
            .collect(Collectors.toList()));

    return "uploadForm";
  }

  @GetMapping("/files/{filename:.+}")
  @ResponseBody
  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

    Resource file = storageService.loadAsResource(filename);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
        .body(file);
  }

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public String handleFileUpload(@RequestParam("uploadingFiles") MultipartFile[] uploadingFiles) {

    List<MultipartFile> notSupportedFiles = new ArrayList<>();

    for (MultipartFile file : uploadingFiles) {
      storageService.store(file);
      String extension = CommonUtil.getFileExtension(new File(file.getOriginalFilename()));
      if (extension.equals("mp3") || extension.equals("wav")) {
        conversionController.analyzeFile(file.getOriginalFilename());
      } else {
        notSupportedFiles.add(file);
      }
    }

    if (!notSupportedFiles.isEmpty()) {
      StringBuilder stringBuilder = new StringBuilder();
      for (MultipartFile file : notSupportedFiles) {
        stringBuilder.append(file.getOriginalFilename());
        stringBuilder.append(" ");
      }
      throw new RuntimeException("Files in unsupported format: " + stringBuilder.toString());
    }
    return "redirect:/";
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
