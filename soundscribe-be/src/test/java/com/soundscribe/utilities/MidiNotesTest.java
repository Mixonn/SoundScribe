package com.soundscribe.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class MidiNotesTest {

  @Test
  void getNoteSymbolByMidiValue() {
    int midiValue = 60;
    assertEquals("C4", MidiNotes.getNoteSymbolByMidiValue(midiValue));

    midiValue = 20;
    assertEquals("-1", MidiNotes.getNoteSymbolByMidiValue(midiValue));
    midiValue--;
    assertEquals("-2", MidiNotes.getNoteSymbolByMidiValue(midiValue));

    midiValue = 128;
    assertEquals("+1", MidiNotes.getNoteSymbolByMidiValue(midiValue));
    midiValue++;
    assertEquals("+2", MidiNotes.getNoteSymbolByMidiValue(midiValue));
  }

  @Test
  void getMidiValueByNoteSymbol() {
    assertEquals(60, MidiNotes.getMidiValueByNoteSymbol("C4"));
    assertEquals(77, MidiNotes.getMidiValueByNoteSymbol("F5"));
  }
}
