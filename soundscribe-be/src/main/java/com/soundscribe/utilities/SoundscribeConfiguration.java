package com.soundscribe.utilities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * Common variables used in project.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "path")
@Getter
@Setter
public class SoundscribeConfiguration {
    private String songDataStorage;
    private String systemLib;
}
