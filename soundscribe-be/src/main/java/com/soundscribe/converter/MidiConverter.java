package com.soundscribe.converter;

import com.soundscribe.utilities.SoundscribeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sound.midi.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts midi to any other music format and creates midi from raw pYIN data.
 */
@Component
public class MidiConverter {

  @Autowired
  private SoundscribeConfiguration soundscribeConfiguration;

  /**
   * Directly converts pYIN data to Midi file. Midi sounds correct but BPM is always set to 120.
   * Best way is to use MusicXML converter first.
   *
   * @param fileXML Xml file with notes.
   * @return Created from notes midi file.
   */
  public File convertXmlToMidi(File fileXML) {
    File midiFile = null;
    int PPQ = 24;
    int BPM = 120; // 120 standard midi BPM
    XmlPojo xml = readXMLData(fileXML);

    try {
      Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
      Track track = sequence.createTrack();
      // Adding notes
      int tickToStart, ticksToStop;
      for (NotePojo note : xml.getNotes()) {
        // Add Note On event
        tickToStart = secondsToTicks(BPM, PPQ, note.timestamp);
        track.add(makeEvent(144, 1, note.midiValue, 96, tickToStart));
        // Add Note Off event
        ticksToStop = secondsToTicks(BPM, PPQ, note.duration);
        track.add(makeEvent(128, 1, note.midiValue, 96, tickToStart + ticksToStop));
      }

      // write MIDI
      midiFile = new File(soundscribeConfiguration.getSongDataStorage() + xml.getName() + ".mid");
      MidiSystem.write(sequence, 1, midiFile);

    } catch (Exception e) {
      System.out.println("Error while creating MIDI file.");
      e.printStackTrace();
    }
    return midiFile;
  }

  /**
   * Creates midi event.
   *
   * @param command
   * @param channel
   * @param note
   * @param velocity
   * @param tick
   * @return
   */
  private MidiEvent makeEvent(int command, int channel, int note, int velocity, int tick) {

    MidiEvent event = null;
    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(command, channel, note, velocity);
      event = new MidiEvent(a, tick);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return event;
  }

  /**
   * Calculate length of notes in ticks.
   *
   * @param BPM beat per minute
   * @param PPQ Midi file parameter
   * @param time Length of note in seconds
   * @return number of ticks
   */
  private int secondsToTicks(int BPM, int PPQ, double time) {
    double tickTimeInMs = (double) 60000 / (BPM * PPQ);
    return (int) (time * 1000 / tickTimeInMs);
  }

  /**
   * Reads data from xml file and stores it into object for next processing.
   *
   * @param fileXML Xml file with notes.
   * @return XmlPojo object with parsed data.
   */
  private XmlPojo readXMLData(File fileXML) {
    XmlPojo xmlPojo = new XmlPojo();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      System.out.println("Error while converting XML.");
      e.printStackTrace();
    }
    Document document = null;
    try {
      document = builder.parse(fileXML);
    } catch (SAXException e) {
      System.out.println("Invalid xml file format");
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Could not find xml file.");
      e.printStackTrace();
    }
    document.getDocumentElement().normalize();

    Element root = document.getDocumentElement();
    String songName = root.getNodeName();
    xmlPojo.setName(songName);

    List<NotePojo> noteList = new ArrayList<>();
    NodeList nList = document.getElementsByTagName("note");
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node node = nList.item(temp);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) node;

        double timestamp =
            Double.parseDouble(eElement.getElementsByTagName("timestamp").item(0).getTextContent());
        double duration =
            Double.parseDouble(eElement.getElementsByTagName("duration").item(0).getTextContent());
        double value =
            Double.parseDouble(eElement.getElementsByTagName("value").item(0).getTextContent());
        int midiValue =
            Integer.parseInt(eElement.getElementsByTagName("midiValue").item(0).getTextContent());
        String letterNotde = eElement.getElementsByTagName("letterNote").item(0).getTextContent();
        noteList.add(new NotePojo(timestamp, duration, value, midiValue, letterNotde));
      }
    }
    xmlPojo.setNotes(noteList);
    return xmlPojo;
  }
}
