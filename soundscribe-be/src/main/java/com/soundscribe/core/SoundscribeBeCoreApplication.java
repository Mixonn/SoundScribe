package com.soundscribe.core;

import com.soundscribe.jvamp.JvampService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.FileSystemXmlApplicationContext;

@SpringBootApplication
public class SoundscribeBeCoreApplication {

  public static void main(String[] args) {
    ApplicationContext applicationContext = new FileSystemXmlApplicationContext("soundscribe-be/Beans.xml");
    SpringApplication.run(SoundscribeBeCoreApplication.class, args);

  }

  @Bean
  static JvampService jvampService() {
    return new JvampService();
  }
}
