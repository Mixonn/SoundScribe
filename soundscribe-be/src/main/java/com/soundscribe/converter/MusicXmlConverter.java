package com.soundscribe.converter;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.time.Duration;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusicXmlConverter {

  private final SoundscribeConfiguration soundscribeConfiguration;

  public File convertXmlToMidi(File xml)
      throws ParserConfigurationException, TransformerException {
    XmlPojo xmlPojo = XmlPojo.readXMLData(xml);
    File musicXmlFile = new File(
        soundscribeConfiguration.getSongDataStorage() + xmlPojo.getName() + ".musicxml");

    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();

    Element scorePartwise = document.createElement("score-partwise");
    document.appendChild(scorePartwise);

    ////////////////////
    Element partList = document.createElement("part-list");
    scorePartwise.appendChild(partList);

    Element scorePart = document.createElement("score-part");
    scorePart.setAttribute("id", "P1");
    partList.appendChild(scorePart);

    Element partName = document.createElement("part-name");
    partName.appendChild(document.createTextNode("Music"));
    scorePart.appendChild(partName);

    ////////////////////

    Element part = document.createElement("part");
    part.setAttribute("id", "P1");
    scorePartwise.appendChild(part);

    Element measure = document.createElement("measure");
    part.setAttribute("number", "1");
    part.appendChild(measure);

    measure.appendChild(createAttributes(document));
    addNotesToMusicXml(measure, document, xmlPojo);

    // create the xml file
    // transform the DOM Object to an XML File
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(document);
    StreamResult streamResult = new StreamResult(musicXmlFile);

    transformer.transform(domSource, streamResult);
    return null;
  }

  private Element createAttributes(Document document) {
    Element attributes = document.createElement("attributes");

    /////

    Element divisions = document.createElement("divisions");
    divisions.appendChild(document.createTextNode("4"));
    attributes.appendChild(divisions);

    /////

    Element key = document.createElement("key");
    attributes.appendChild(key);

    Element fifths = document.createElement("fifths");
    fifths.appendChild(document.createTextNode("0"));
    key.appendChild(fifths);

    ////

    Element time = document.createElement("time");
    attributes.appendChild(time);

    Element beats = document.createElement("beats");
    beats.appendChild(document.createTextNode("4"));
    time.appendChild(beats);

    Element beatType = document.createElement("beat-type");
    beatType.appendChild(document.createTextNode("4"));
    time.appendChild(beatType);

    /////

    Element clef = document.createElement("clef");
    attributes.appendChild(clef);

    Element sign = document.createElement("sign");
    sign.appendChild(document.createTextNode("G"));
    clef.appendChild(sign);

    Element line = document.createElement("line");
    line.appendChild(document.createTextNode("2"));
    clef.appendChild(line);

    return attributes;
  }

  private double findLongestNote(List<NotePojo> noteList) {
    double longestNoteTime = 0;
    for (NotePojo notePojo : noteList) {
      if (notePojo.getDuration() > longestNoteTime) {
        longestNoteTime = notePojo.getDuration();
      }
    }
    return longestNoteTime;
  }

  private double[] calculateNotesTimes(List<NotePojo> noteList) {
    double[] times = {0, 0, 0, 0, 0};
    times[0] = findLongestNote(noteList);
    times[1] = times[0] / 2;
    times[2] = times[1] / 2;
    times[3] = times[2] / 2;
    times[4] = times[3] / 2;
    return times;
  }

  private String chooseNoteValue(double duration, double[] times) {
    if (duration > 1.5 * times[1]) {
      return "whole";
    } else if (duration > 1.5 * times[2]) {
      return "half";
    } else if (duration > 1.5 * times[3]) {
      return "quarter";
    } else if (duration > 1.5 * times[4]) {
      return "eighth";
    } else {
      return "16th";
    }
  }

  private void addNotesToMusicXml(Element measure, Document document, XmlPojo xmlPojo) {
    double timeNow = 0;
    for (NotePojo note : xmlPojo.getNotes()) {

      double noteStartTime = note.getTimestamp();
      double timeForRest = noteStartTime-timeNow;

      if(timeForRest>0) {
        double noteTime[] = calculateNotesTimes(xmlPojo.getNotes());
        String typeValue = chooseNoteValue(note.getDuration(), noteTime);
        String durationValue;
        if (typeValue == "whole") {
          durationValue = "16";
        } else if (typeValue == "half") {
          durationValue = "8";
        } else if (typeValue == "quarter") {
          durationValue = "4";
        } else if (typeValue == "eighth") {
          durationValue = "2";
        } else {
          durationValue = "1";
        }
        createPause(measure,document,durationValue,typeValue);
      }

      String stepValue = getStep(note.getLetterNote());
      String octaveValue = getOctave(note.getLetterNote());

      double noteTime[] = calculateNotesTimes(xmlPojo.getNotes());
      String typeValue = chooseNoteValue(note.getDuration(), noteTime);

      String durationValue;
      if (typeValue == "whole") {
        durationValue = "16";
      } else if (typeValue == "half") {
        durationValue = "8";
      } else if (typeValue == "quarter") {
        durationValue = "4";
      } else if (typeValue == "eighth") {
        durationValue = "2";
      } else {
        durationValue = "1";
      }

      crateNote(measure, document, stepValue, octaveValue, durationValue, typeValue);
      timeNow=note.getTimestamp()+note.getDuration();
    }
  }

  private void createPause(Element measure, Document document, String durationValue, String typeValue) {
    Element note = document.createElement("note");
    measure.appendChild(note);

    Element rest = document.createElement("rest");
    note.appendChild(rest);

    Element duration = document.createElement("duration");
    duration.appendChild(document.createTextNode(durationValue));
    note.appendChild(duration);

    Element type = document.createElement("type");
    type.appendChild(document.createTextNode(typeValue));
    note.appendChild(type);

  }

  private void crateNote(Element measure, Document document, String stepValue,
      String octaveValue, String durationValue, String typeValue) {

    Element note = document.createElement("note");
    measure.appendChild(note);
/////
    Element pitch = document.createElement("pitch");
    note.appendChild(pitch);

    Element step = document.createElement("step");
    step.appendChild(document.createTextNode(stepValue));
    pitch.appendChild(step);

    Element octave = document.createElement("octave");
    octave.appendChild(document.createTextNode(octaveValue));
    pitch.appendChild(octave);

    ////

    Element duration = document.createElement("duration");
    duration.appendChild(document.createTextNode(durationValue));
    note.appendChild(duration);

    ////

    Element type = document.createElement("type");
    type.appendChild(document.createTextNode(typeValue));
    note.appendChild(type);
  }

  private String getStep(String noteSymbol) {
    return noteSymbol.substring(0, noteSymbol.length() - 1);
  }

  private String getOctave(String noteSymbol) {
    return noteSymbol.substring(noteSymbol.length() - 1, noteSymbol.length());
  }
}
