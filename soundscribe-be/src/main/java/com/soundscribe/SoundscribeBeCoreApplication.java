package com.soundscribe;

import com.soundscribe.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SoundscribeBeCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(SoundscribeBeCoreApplication.class, args);
  }

  @Bean
  CommandLineRunner init(StorageService storageService) {
    return args -> {
      //storageService.deleteAll();
      storageService.init();
    };
  }
}
