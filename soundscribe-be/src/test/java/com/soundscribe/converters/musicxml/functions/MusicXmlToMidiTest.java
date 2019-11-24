package com.soundscribe.converters.musicxml.functions;

import static org.junit.jupiter.api.Assertions.*;

import com.soundscribe.converters.MidiConverter;
import com.soundscribe.core.AppConfig;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = AppConfig.class)
class MusicXmlToMidiTest {

  @Autowired
  private MidiConverter midiConverter;

  private File musicXmlFile;
  private File midiFile;


  @BeforeAll
  void init() {
    ClassLoader classLoader = getClass().getClassLoader();
    musicXmlFile = new File(classLoader.getResource("example.musicxml").getFile());
  }

  @Test
  void convertMusicXmlToMidi() {
    MusicXmlToMidi musicXmlToMidi = new MusicXmlToMidi(midiConverter);
    musicXmlToMidi.convertMusicXmlToMidi(musicXmlFile);

    String pathToMidi = "test.mid";
    midiFile = new File(pathToMidi);
    assertTrue(midiFile.exists());
  }

  @AfterAll
  void clean(){
    try {
      Files.deleteIfExists(musicXmlFile.toPath());
      Files.deleteIfExists(midiFile.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}