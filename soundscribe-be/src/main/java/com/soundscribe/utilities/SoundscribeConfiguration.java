package com.soundscribe.utilities;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Common variables used in project.
 */
@Data
@Component
@ConfigurationProperties("path")
public class SoundscribeConfiguration {
    private String songDataStorage;
    private String systemLib;
}
