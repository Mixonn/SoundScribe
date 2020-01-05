package com.soundscribe;

import com.soundscribe.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class SoundscribeBeCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(SoundscribeBeCoreApplication.class, args);
  }

  @Bean
  CommandLineRunner init(StorageService storageService) {
    return args -> {
      // storageService.deleteAll();
      storageService.init();
    };
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
      }
    };
  }
}
