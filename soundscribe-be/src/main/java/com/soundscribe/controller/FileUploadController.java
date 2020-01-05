package com.soundscribe.controller;

import com.soundscribe.OnlyForShow;
import com.soundscribe.storage.StorageFileNotFoundException;
import com.soundscribe.storage.StorageService;
import java.io.IOException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {

  private final StorageService storageService;
  private final OnlyForShow onlyForShow;

  @Autowired
  public FileUploadController(StorageService storageService, OnlyForShow onlyForShow) {
    this.storageService = storageService;
    this.onlyForShow = onlyForShow;
  }

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

  @PostMapping("/")
  public String handleFileUpload(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    storageService.store(file);
    onlyForShow.analyzeFile(file.getOriginalFilename()); // todo Delete this line

    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + file.getOriginalFilename() + "!");

    return "redirect:/";
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
