package com.soundscribe.utilities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Common variables used in project.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties("musicxml")
@PropertySource("classpath:application.yml")
public class MusicXmlConfiguration {
  private boolean useBassKey;
  private boolean useDots;
  private boolean useRest;

  private boolean delete16thRest;
  private boolean delete16thPitch;
}
