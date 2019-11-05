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
@ConfigurationProperties("path")
@PropertySource("classpath:application.yml")
public class SoundscribeConfiguration {
  private String songDataStorage;
  private String systemLib;
}
