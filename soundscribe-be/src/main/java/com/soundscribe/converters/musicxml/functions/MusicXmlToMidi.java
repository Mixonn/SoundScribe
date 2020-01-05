package com.soundscribe.converters.musicxml.functions;

import com.soundscribe.converters.PyinNote;
import com.soundscribe.converters.XmlConverter;
import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import com.soundscribe.converters.musicxml.utilities.MusicXmlUtils;
import com.soundscribe.converters.xml.XmlPojo;
import com.soundscribe.utilities.MidiNotes;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
@Component
public class MusicXmlToMidi {

  private final XmlConverter xmlConverter;

  public File convertMusicXmlToMidi(File musicXml) {
    XmlPojo xmlPojo = musicXmlToXmlPojo(musicXml);
    return xmlConverter.convertXmlToMidi(xmlPojo);
  }

  /**
   * Parse musicXml data to XmlPojo object
   *
   * @param musicXml MusicXml file
   * @return XmlPojo object
   */
  private XmlPojo musicXmlToXmlPojo(File musicXml) {
    MusicXmlUtils musicXmlUtils = new MusicXmlUtils();
    XmlPojo xmlPojo = new XmlPojo();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    try {
      builder = factory.newDocumentBuilder();
      document = builder.parse(musicXml);
    } catch (Exception e) {
      log.error("Error while converting musicxml to xml.", e);
      throw new RuntimeException(e);
    }
    document.getDocumentElement().normalize();

    try {
      String title = document.getElementsByTagName("movement-title").item(0).getTextContent();
      xmlPojo.setSongName(title);
    } catch (NullPointerException e) {
      xmlPojo.setSongName(musicXml.getName());
    }

    Element soundElement = (Element) document.getElementsByTagName("sound").item(0);
    int bpm = (int) Double.parseDouble(soundElement.getAttribute("tempo"));
    xmlPojo.setBpm(bpm);

    Element divisionsElement = (Element) document.getElementsByTagName("divisions").item(0);
    int divisions = Integer.parseInt(divisionsElement.getTextContent());
    xmlPojo.setDivisions(divisions);

    List<MusicXmlNote> noteTimes = musicXmlUtils.getMusicXmlBaseNotes(bpm, divisions);

    List<PyinNote> noteList = new ArrayList<>();
    NodeList nList = document.getElementsByTagName("note");
    double timeNow = 0;
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node node = nList.item(temp);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) node;

        int duration =
            Integer.parseInt(eElement.getElementsByTagName("duration").item(0).getTextContent());
        double durationInSeconds = musicXmlUtils.getNoteDurationInSeconds(duration, noteTimes);

        if (eElement.getElementsByTagName("pitch").getLength() > 0) {
          Element pitchElement = (Element) eElement.getElementsByTagName("pitch").item(0);
          String pitchValue = pitchElement.getElementsByTagName("step").item(0).getTextContent();
          String octaveValue = pitchElement.getElementsByTagName("octave").item(0).getTextContent();
          String letterNote = pitchValue + octaveValue;
          int midiNote = MidiNotes.getMidiValueByNoteSymbol(letterNote);
          noteList.add(new PyinNote(timeNow, durationInSeconds, 0, midiNote, letterNote));
        } else {
          noteList.add(new PyinNote(timeNow, durationInSeconds, 0, 0, null));
        }
        timeNow += durationInSeconds;
      }
    }
    xmlPojo.setNotes(noteList);

    return xmlPojo;
  }
}
