package com.soundscribe.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MidiNotesTest {
  @Test
  public void midiValueInCorrectRange() {
    int midiValue = 60;
    assertEquals("C4", MidiNotes.getNoteSymbolByMidiValue(midiValue));
  }

  @Test
  public void midiValueToSmall() {
    int midiValue = 20;
    assertEquals("-1", MidiNotes.getNoteSymbolByMidiValue(midiValue));
    midiValue--;
    assertEquals("-2", MidiNotes.getNoteSymbolByMidiValue(midiValue));
  }

  @Test
  public void midiValueToLarge() {
    int midiValue = 128;
    assertEquals("+1", MidiNotes.getNoteSymbolByMidiValue(midiValue));
    midiValue++;
    assertEquals("+2", MidiNotes.getNoteSymbolByMidiValue(midiValue));
  }
}
