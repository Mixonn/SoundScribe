package com.soundscribe.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@SpringBootApplication
public class SoundscribeBeCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(SoundscribeBeCoreApplication.class, args);
  }

}
