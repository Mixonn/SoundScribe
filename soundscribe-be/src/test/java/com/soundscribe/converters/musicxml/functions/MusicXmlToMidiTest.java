package com.soundscribe.converters.musicxml.functions;

import com.soundscribe.converters.XmlConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
class MusicXmlToMidiTest {

  @Autowired
  private XmlConverter xmlConverter;

  private File musicXmlFile;
  private File midiFile;

  @BeforeAll
  void init() {
    ClassLoader classLoader = getClass().getClassLoader();
    musicXmlFile = new File(classLoader.getResource("samples/example.mxl").getFile());
  }

  @Test
  void convertMusicXmlToMidi() {
    MusicXmlToMidi musicXmlToMidi = new MusicXmlToMidi(xmlConverter);
    musicXmlToMidi.convertMusicXmlToMidi(musicXmlFile);

    // TODO @Krysu: fix this test
    String pathToMidi = "test.mid";
    midiFile = new File(pathToMidi);
    assertTrue(midiFile.exists());
  }

  @AfterAll
  void clean() {
    try {
      Files.deleteIfExists(midiFile.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
