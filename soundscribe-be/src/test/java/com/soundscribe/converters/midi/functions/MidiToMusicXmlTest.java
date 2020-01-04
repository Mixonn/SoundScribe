package com.soundscribe.converters.midi.functions;

import static org.junit.jupiter.api.Assertions.*;

import com.soundscribe.converters.MidiConverter;
import com.soundscribe.converters.xml.functions.XmlToMusicXml;
import com.soundscribe.utilities.SoundscribeConfiguration;
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
class MidiToMusicXmlTest {

  @Autowired private MidiConverter midiConverter;

  private File midiFile;
  private File musicXmlFile;

  @BeforeAll
  void init() {
    ClassLoader classLoader = getClass().getClassLoader();
    midiFile = new File(classLoader.getResource("samples/example.mid").getFile());
  }

  @Test
  void convertMidiToMusicXml() {
    MidiToMusicXml midiToMusicXml =
        new MidiToMusicXml(new XmlToMusicXml(new SoundscribeConfiguration()));
    musicXmlFile = midiToMusicXml.convertMidiToMusicXml(midiFile);
    assertTrue(musicXmlFile.exists());
  }

  @AfterAll
  void clean() {
    try {
      Files.deleteIfExists(musicXmlFile.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
