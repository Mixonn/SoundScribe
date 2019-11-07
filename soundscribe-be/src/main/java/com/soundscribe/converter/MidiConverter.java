package com.soundscribe.converter;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Converts midi to any other music format and creates midi from raw pYIN data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MidiConverter {

  private final SoundscribeConfiguration soundscribeConfiguration;

  /**
   * Directly converts pYIN data to Midi file. Midi sounds correct but bpm is always set to 120.
   * Best way is to use MusicXML converter first.
   *
   * @param fileXML Xml file with notes.
   * @return Created from notes midi file.
   */
  public File convertXmlToMidi(File fileXML) {
    File midiFile = null;
    int ppq = 24;
    int bpm = 120; // 120 standard midi bpm
    XmlPojo xml = XmlPojo.readXMLData(fileXML);

    try {
      Sequence sequence = new Sequence(Sequence.PPQ, ppq);
      Track track = sequence.createTrack();
      // Adding notes
      int tickToStart, ticksToStop;
      for (NotePojo note : xml.getNotes()) {
        // Add Note On event
        tickToStart = secondsToTicks(bpm, ppq, note.getTimestamp());
        track.add(makeEvent(144, 1, note.getMidiValue(), 96, tickToStart));
        // Add Note Off event
        ticksToStop = secondsToTicks(bpm, ppq, note.getDuration());
        track.add(makeEvent(128, 1, note.getMidiValue(), 96, tickToStart + ticksToStop));
      }

      // write MIDI
      midiFile = new File(soundscribeConfiguration.getSongDataStorage() + xml.getName() + ".mid");
      MidiSystem.write(sequence, 1, midiFile);

    } catch (Exception e) {
      log.error("Error while creating MIDI file.",e);
      throw new RuntimeException(e);
    }
    return midiFile;
  }

  /**
   * Creates midi event.
   *
   * @param command  Type of event for example. 144 - note start, 128 - note stop
   * @param channel
   * @param note     Midi value of note
   * @param velocity A measure of how rapidly and forcefully a key on a keyboard is pressed
   * @param tick     Time in ticks when event will happen
   * @return
   */
  private MidiEvent makeEvent(int command, int channel, int note, int velocity, int tick) {

    MidiEvent event = null;
    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(command, channel, note, velocity);
      event = new MidiEvent(a, tick);
    } catch (Exception e) {
      log.error("Error while creating midi event",e);
      throw new RuntimeException(e);
    }
    return event;
  }

  /**
   * Calculate length of notes in ticks.
   *
   * @param bpm  beat per minute
   * @param ppq  Midi file parameter
   * @param time Length of note in seconds
   * @return number of ticks
   */
  private int secondsToTicks(int bpm, int ppq, double time) {
    double tickTimeInMs = (double) 60000 / (bpm * ppq);
    return (int) (time * 1000 / tickTimeInMs);
  }
}
