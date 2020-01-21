package com.soundscribe.converters;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class XmlConverterTest {

  private File xmlFile;
  private File midiFile;

  @BeforeAll
  void setUp() {
    ClassLoader classLoader = getClass().getClassLoader();
    xmlFile = new File(classLoader.getResource("samples/TP0300A_04.xml").getFile());
  }

  @AfterAll
  void tearDown() {
    if (xmlFile != null) {
      try {
        Files.delete(xmlFile.toPath());
      } catch (IOException ignored) {
      }
    }
    if (midiFile != null) {
      try {
        Files.delete(midiFile.toPath());
      } catch (IOException ignored) {
      }
    }
  }

  @Test
  void testConvertXmlToMidi() throws IOException {
    XmlConverter xmlConverter = new XmlConverter(new SoundscribeConfiguration());
    midiFile = xmlConverter.convertXmlToMidi(xmlFile);
    assertTrue(midiFile.exists());
  }
}
