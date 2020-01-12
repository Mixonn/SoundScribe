package com.soundscribe.controller;

import com.soundscribe.storage.StorageFileNotFoundException;
import com.soundscribe.storage.StorageService;
import com.soundscribe.utilities.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class FileUploadController {

  private final StorageService storageService;
  private final ConversionController conversionController;

  @GetMapping("/")
  @PreAuthorize("hasAuthority('SCOPE_soundscribe-edit')")
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
  @PreAuthorize("hasAuthority('SCOPE_soundscribe-edit')")
  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

    Resource file = storageService.loadAsResource(filename);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
        .body(file);
  }

  @PostMapping("/")
  @PreAuthorize("hasAuthority('SCOPE_soundscribe-edit')")
  public String handleFileUpload(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    storageService.store(file);
    String extension = CommonUtil.getFileExtension(new File(file.getOriginalFilename()));
    if (extension.equals("mp3") || extension.equals("wav")) {
      conversionController.analyzeFile(file.getOriginalFilename());
    } else {
      throw new RuntimeException("File in unsupported format");
    }

    redirectAttributes.addFlashAttribute(
        "message", "You successfully uploaded " + file.getOriginalFilename() + "!");

    return "redirect:/";
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }
}
