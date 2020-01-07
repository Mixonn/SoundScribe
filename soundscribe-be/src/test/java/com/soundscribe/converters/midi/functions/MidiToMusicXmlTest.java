package com.soundscribe.converters.midi.functions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soundscribe.converters.xml.functions.XmlToMusicXml;
import com.soundscribe.utilities.MusicXmlConfiguration;
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
class MidiToMusicXmlTest {

  private File midiFile;
  private File musicXmlFile;

  @BeforeAll
  void init() {
    ClassLoader classLoader = getClass().getClassLoader();
    midiFile = new File(classLoader.getResource("samples/example.midi").getFile());
  }

  @Test
  void convertMidiToMusicXml() {
    MidiToMusicXml midiToMusicXml =
        new MidiToMusicXml(
            new XmlToMusicXml(new SoundscribeConfiguration(), new MusicXmlConfiguration()));
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
