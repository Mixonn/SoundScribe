package com.soundscribe.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Setter
@Configuration
@ConfigurationProperties("storage")
@PropertySource("classpath:application.yml")
public class StorageProperties {

  /**
   * Folder location for storing files
   */
  private String location = "audioFilesStorage";

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

}
