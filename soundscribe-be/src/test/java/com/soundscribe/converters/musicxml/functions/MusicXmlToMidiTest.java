package com.soundscribe.converters.musicxml.functions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soundscribe.converters.MidiConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
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