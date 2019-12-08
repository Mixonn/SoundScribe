package com.soundscribe.converters.midi.functions;

import com.soundscribe.converters.PyinNote;
import com.soundscribe.converters.xml.XmlPojo;
import com.soundscribe.converters.xml.functions.XmlToMusicXml;
import com.soundscribe.utilities.CommonUtil;
import com.soundscribe.utilities.MidiNotes;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MidiToMusicXml {
  private static int SET_TEMPO = 81;
  private static int NOTE_OFF = 128;
  private static int NOTE_ON = 144;

  private final XmlToMusicXml xmlToMusicXml;

  /**
   * This function converts the number of ticks into seconds.
   *
   * @param bpm Song tempo
   * @param ppq Divisions per tick for midi file
   * @param ticks
   * @return
   */
  private double ticksToSeconds(int bpm, int ppq, double ticks) {
    double timeInMs = ticks * (60000.0 / (bpm * ppq));
    return timeInMs / 1000;
  }

  /**
   * Converts midi file to musicxml file.
   *
   * @param midiFile midi file
   * @return musicXml file
   */
  public File convertMidiToMusicXml(File midiFile) {
    File musicXml = null;
    try {
      XmlPojo xmlPojo = convertMidiToXmlPojo(midiFile);
      musicXml = xmlToMusicXml.convertXmlToMusicXml(xmlPojo);
    } catch (Exception e) {
      log.error("", e);
    }
    return musicXml;
  }

  /**
   * Converts midi file to temporary XmlPojo object.
   *
   * @param midiFile midi file
   * @return XmlPojo object
   */
  private XmlPojo convertMidiToXmlPojo(File midiFile)
      throws InvalidMidiDataException, IOException, ParseException {
    Sequence sequence = MidiSystem.getSequence(midiFile);
    Track track = sequence.getTracks()[0];

    XmlPojo xmlPojo = new XmlPojo();
    xmlPojo.setSongName(CommonUtil.getFileNameWithoutExtension(midiFile));
    xmlPojo.setDivisions(sequence.getResolution());

    List<PyinNote> noteList = new ArrayList<>();
    boolean noteStarted = false;
    long noteStartTick = 0;
    for (int i = 0; i < track.size(); i++) {
      MidiEvent midiEvent = track.get(i);
      MidiMessage midiMessage = midiEvent.getMessage();
      if (midiMessage instanceof ShortMessage) {
        ShortMessage sm = (ShortMessage) midiMessage;
        int command = sm.getCommand();
        if (command == NOTE_ON && !noteStarted) {
          int midiNote = sm.getData1();
          if (midiNote < 21) continue;
          noteStarted = true;
          noteStartTick = midiEvent.getTick();

          PyinNote pyinNote = new PyinNote();
          pyinNote.setMidiValue(midiNote);
          pyinNote.setLetterNote(MidiNotes.getNoteSymbolByMidiValue(midiNote));
          pyinNote.setTimestamp(
              ticksToSeconds(xmlPojo.getBpm(), xmlPojo.getDivisions(), noteStartTick));
          noteList.add(pyinNote);
        } else if (command == NOTE_OFF && noteStarted) {
          noteStarted = false;
          long durationInTicks = midiEvent.getTick() - noteStartTick;
          double durationInSeconds =
              ticksToSeconds(xmlPojo.getBpm(), xmlPojo.getDivisions(), durationInTicks);
          noteList.get(noteList.size() - 1).setDurationInSeconds(durationInSeconds);
        } else if (command == NOTE_OFF || command == NOTE_ON) {
          // throw new ParseException("Failed to load notes from midi file", 0);
        }
      } else if (midiMessage instanceof MetaMessage) {
        MetaMessage mm = (MetaMessage) midiMessage;
        if (mm.getType() == SET_TEMPO) {
          loadBpmFromMetaMessageToXmlPojo(mm, xmlPojo);
        }
      }
    }
    xmlPojo.setNotes(noteList);
    return xmlPojo;
  }

  /**
   * Reads value of tempo from midi file and saves it to temporary object.
   *
   * @param mm Meta Message with set tempo command
   * @param xmlPojo temporary XmlPojo object
   * @throws ParseException
   */
  private void loadBpmFromMetaMessageToXmlPojo(MetaMessage mm, XmlPojo xmlPojo)
      throws ParseException {
    int tempo = 0;
    for (int j = 0; j < 2; j++) {
      int shift = (3 - 1 - j) * 8;
      tempo += (mm.getData()[j] << shift);
    }
    if (tempo == 0) {
      throw new ParseException("Failed to load bpm value from midi file", 0);
    }
    Integer bpm = 60000000 / tempo;
    xmlPojo.setBpm(bpm);
  }
}
