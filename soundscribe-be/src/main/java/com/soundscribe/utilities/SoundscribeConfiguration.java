package com.soundscribe.utilities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/** Common variables used in project. */
@Getter
@Setter
@Configuration
@ConfigurationProperties("settings")
@PropertySource("classpath:application.yml")
public class SoundscribeConfiguration {
  private String uploadDirectory;
  private String downloadDirectory;
  private String pathToDLibraScript;

  private String songDataStorage;
  private String vampPath;
  private int defaultDivisions;
  private int defaultBpm;
  private int defaultBeats;
  private int defaultBeatType;

  private int maxNotesInLineAbc;
}
