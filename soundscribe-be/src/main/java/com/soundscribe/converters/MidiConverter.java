package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Converts midi to any other music format and creates midi from raw pYIN data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MidiConverter {

  private final SoundscribeConfiguration soundscribeConfiguration;

  public File convertXmlToMidi(XmlPojo xml) {
    return convertXmlPojoToMidi(xml);
  }

  /**
   * Directly converts pYIN data to Midi file. Midi sounds correct but bpm is always set to 120.
   * Best way is to use MusicXML converter first.
   *
   * @param fileXML Xml file with notes.
   * @return Created from notes midi file.
   */
  public File convertXmlToMidi(File fileXML) {
    XmlPojo xml = XmlPojo.readXMLData(fileXML);
    xml.setDivisions(soundscribeConfiguration.getDefaultDivisions());
    xml.setBpm(soundscribeConfiguration.getDefaultBpm());
    return convertXmlPojoToMidi(xml);
  }

  /**
   * Creates midi file from XmlPojo object.
   *
   * @param xml XmlPojo object that contains information about song
   * @return Midi file
   */
  private File convertXmlPojoToMidi(XmlPojo xml) {
    File midiFile;
    int ppq = xml.getDivisions();
    int bpm = xml.getBpm();
    try {
      Sequence sequence = new Sequence(Sequence.PPQ, ppq);
      Track track = sequence.createTrack();
      track.add(createSetTempoEvent(0, bpm));
      // Adding notes
      int tickToStart, ticksToStop;
      for (PyinNote note : xml.getNotes()) {
        // Add Note On event
        tickToStart = secondsToTicks(bpm, ppq, note.getTimestamp());
        track.add(makeEvent(144, 1, note.getMidiValue(), 96, tickToStart));
        // Add Note Off event
        ticksToStop = secondsToTicks(bpm, ppq, note.getDurationInSeconds());
        track.add(makeEvent(128, 1, note.getMidiValue(), 96, tickToStart + ticksToStop));
      }

      // write MIDI
      midiFile = new File(
          soundscribeConfiguration.getSongDataStorage() + xml.getSongName() + ".mid");
      MidiSystem.write(sequence, 1, midiFile);

    } catch (Exception e) {
      log.error("Error while creating MIDI file.", e);
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
      log.error("Error while creating midi event", e);
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

  /**
   * Create a Set Tempo meta event. Takes a tempo in BPMs.
   *
   * @param tick  Tick in which tempo will change
   * @param tempo BPM value
   * @return MidiEvent which changes tempo
   */
  private MidiEvent createSetTempoEvent(long tick, long tempo) {
    // microseconds per quarternote
    long mpqn = 60000000 / tempo;

    MetaMessage metaMessage = new MetaMessage();

    // create the tempo byte array
    byte[] array = new byte[]{0, 0, 0};

    for (int i = 0; i < 3; i++) {
      int shift = (3 - 1 - i) * 8;
      array[i] = (byte) (mpqn >> shift);
    }

    // now set the message
    try {
      metaMessage.setMessage(81, array, 3);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    return new MidiEvent(metaMessage, tick);
  }
}
