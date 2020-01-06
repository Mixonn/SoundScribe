package com.soundscribe.converters.musicxml.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class MusicXmlUtilsTest {

  @Test
  void getMusicXmlBaseNotes() {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils();

    List<MusicXmlNote> list = musicXmlUtils.getMusicXmlBaseNotes(120, 4);
    assertEquals("half", list.get(3).getName());
    assertEquals(1, list.get(3).getSeconds());
    assertEquals(8, list.get(3).getDuration());
    assertFalse(list.get(3).isWithDot());

    assertEquals("quarter", list.get(4).getName());
    assertEquals(0.75, list.get(4).getSeconds());
    assertEquals(6, list.get(4).getDuration());
    assertTrue(list.get(4).isWithDot());

    assertEquals("quarter", list.get(5).getName());
    assertEquals(0.5, list.get(5).getSeconds());
    assertEquals(4, list.get(5).getDuration());
    assertFalse(list.get(5).isWithDot());

    assertEquals("eighth", list.get(6).getName());
    assertEquals(0.375, list.get(6).getSeconds());
    assertEquals(3, list.get(6).getDuration());
    assertTrue(list.get(6).isWithDot());
  }

  @Test
  void chooseBestNoteByDurationInSeconds() {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils();
    List<MusicXmlNote> list = musicXmlUtils.getMusicXmlBaseNotes(120, 4);

    MusicXmlNote note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.0d, list, false, false);
    assertEquals("half", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.2d, list, false, false);
    assertEquals("half", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.3d, list, false, false);
    assertEquals("half", note.getName());
    assertTrue(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(1.7d, list, false, false);
    assertEquals("whole", note.getName());
    assertFalse(note.isWithDot());

    note = musicXmlUtils.chooseBestNoteByDurationInSeconds(0.7d, list, false, false);
    assertEquals("quarter", note.getName());
    assertTrue(note.isWithDot());
  }

  @Test
  void getNoteDurationInSeconds() {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils();
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
