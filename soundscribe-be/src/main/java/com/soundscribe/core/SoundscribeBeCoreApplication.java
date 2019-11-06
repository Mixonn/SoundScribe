package com.soundscribe.core;

import com.soundscribe.utilities.SoundscribeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SoundscribeConfiguration.class)
public class SoundscribeBeCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(SoundscribeBeCoreApplication.class, args);
  }
}
