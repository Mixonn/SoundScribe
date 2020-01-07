package com.soundscribe.converters.musicxml.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import com.soundscribe.utilities.MusicXmlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class MusicXmlUtilsTest {

  @Test
  void getMusicXmlBaseNotes() {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils(new MusicXmlConfiguration());

    List<MusicXmlNote> list = musicXmlUtils.getMusicXmlBaseNotes(120, 4);
    MusicXmlNote note = list.get(1);
    assertEquals("half", note.getName());
    assertEquals(1, note.getSeconds());
    assertEquals(8, note.getDuration());
    assertFalse(note.isWithDot());

    note = list.get(2);
    assertEquals("quarter", note.getName());
    assertEquals(0.5, note.getSeconds());
    assertEquals(4, note.getDuration());
    assertFalse(note.isWithDot());

    note = list.get(3);
    assertEquals("eighth", note.getName());
    assertEquals(0.25, note.getSeconds());
    assertEquals(2, note.getDuration());
    assertFalse(note.isWithDot());
  }

  @Test
  void chooseBestNoteByDurationInSeconds() {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils(new MusicXmlConfiguration());
    List<MusicXmlNote> list = musicXmlUtils.getMusicXmlBaseNotes(120, 4);

    MusicXmlNote note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.0d, list, false);
    assertEquals("half", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.2d, list, false);
    assertEquals("half", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.3d, list, false);
    assertEquals("half", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.7d, list, false);
    assertEquals("whole", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(0.7d, list, false);
    assertEquals("quarter", note.getName());
    assertFalse(note.isWithDot());
  }

  @Test
  void getNoteDurationInSeconds() {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils(new MusicXmlConfiguration());
    List<MusicXmlNote> list = musicXmlUtils.getMusicXmlBaseNotes(120, 4);

    double seconds = musicXmlUtils.getNoteDurationInSeconds(4, list);
    assertEquals(0.5d, seconds);

    seconds = musicXmlUtils.getNoteDurationInSeconds(2, list);
    assertEquals(0.25d, seconds);

    seconds = musicXmlUtils.getNoteDurationInSeconds(8, list);
    assertEquals(1d, seconds);
  }

  @Test
  void getSongNameFromMusicxmlFile() {
    String result = null;
    ClassLoader classLoader = getClass().getClassLoader();
    File musicXmlFile = new File(classLoader.getResource("example.musicxml").getFile());
    try {
      result = MusicXmlUtils.getSongNameFromMusicxmlFile(musicXmlFile);
    } catch (IOException ignored) {
    }
    assertEquals("test", result);
  }
}
