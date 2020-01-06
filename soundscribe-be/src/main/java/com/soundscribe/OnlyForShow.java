package com.soundscribe;

import com.soundscribe.controller.ConversionController;
import com.soundscribe.converters.ConverterService;
import com.soundscribe.jvamp.JvampService;
import com.soundscribe.utilities.SoundscribeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
// todo Delete this class
public class OnlyForShow {
  private final JvampService jvampService;
  private final ConverterService converterService;
  private final SoundscribeConfiguration soundscribeConfiguration;
  private final ConversionController conversionController;

  public void analyzeFile(String filename) {
    conversionController.analyzeFile(filename);
  }
}
