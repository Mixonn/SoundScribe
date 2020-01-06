package com.soundscribe.converters.musicxml.functions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soundscribe.converters.XmlConverter;
import com.soundscribe.converters.musicxml.utilities.MusicXmlNoteUtils;
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
class MusicXmlToMidiTest {

  private File musicXmlFile;
  private File midiFile;

  @BeforeAll
  void init() {
    ClassLoader classLoader = getClass().getClassLoader();
    musicXmlFile = new File(classLoader.getResource("samples/example.mxl").getFile());
  }

  @Test
  void convertMusicXmlToMidi() {
    MusicXmlToMidi musicXmlToMidi =
        new MusicXmlToMidi(
            new XmlConverter(new SoundscribeConfiguration()),
            new MusicXmlNoteUtils(new MusicXmlConfiguration()));
    midiFile = musicXmlToMidi.convertMusicXmlToMidi(musicXmlFile);
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
