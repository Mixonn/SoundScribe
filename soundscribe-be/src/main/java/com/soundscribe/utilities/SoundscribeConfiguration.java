package com.soundscribe.utilities;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Common variables used in project.
 */
@Data
@Component
public class SoundscribeConfiguration {
    private String songDataStorage;
    private String systemLib;
}
