package com.soundscribe.converter;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.Data;
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
  private int bpm = 60;
  private int divisionsValue = 4;
  private int beatsValue = 4;
  private int beatTypeValue = 4;

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
    addTempo(measure, document, bpm);
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

    Element divisions = document.createElement("divisions");
    divisions.appendChild(document.createTextNode(String.valueOf(divisionsValue)));
    attributes.appendChild(divisions);

    Element time = document.createElement("time");
    attributes.appendChild(time);

    Element beats = document.createElement("beats");
    beats.appendChild(document.createTextNode(String.valueOf(beatsValue)));
    time.appendChild(beats);

    Element beatType = document.createElement("beat-type");
    beatType.appendChild(document.createTextNode(String.valueOf(beatTypeValue)));
    time.appendChild(beatType);

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

  @Data
  private static class NoteLengthSettings {

    String name;
    int lengthDuration;
    int lengthSeconds;
  }

  private NoteLengthSettings chooseNoteValues(double duration) {
    NoteLengthSettings noteLengthSettings = null;
    Map<String, Integer> notesLengthInDuration = getNotesLengthInDuration(divisionsValue);
    Map<String, Double> notesLengthInSeconds = getNotesLengthInSeconds(bpm);
    for (Map.Entry<String, Double> entry : notesLengthInSeconds.entrySet()) {
      if (duration > entry.getValue() / 1.5) {
        noteLengthSettings = new NoteLengthSettings();
        noteLengthSettings.setName(entry.getKey());
        noteLengthSettings.setLengthDuration(notesLengthInDuration.get(entry.getKey()));
        break;
      }
    }
    return noteLengthSettings;
  }

  private void addNotesToMusicXml(Element measure, Document document, XmlPojo xmlPojo) {
    int numberOfNotes = xmlPojo.getNotes().size();
    for (int i = 0; i < numberOfNotes; i++) {
      NotePojo note = xmlPojo.getNotes().get(i);

      String stepValue = getStep(note.getLetterNote());
      String octaveValue = getOctave(note.getLetterNote());
      NoteLengthSettings noteLengthSettings = chooseNoteValues(note.getDuration());

      crateNote(measure, document, stepValue, octaveValue, noteLengthSettings.lengthDuration,
          noteLengthSettings.name);


      if (i < numberOfNotes - 1) {
        NotePojo nextNote = xmlPojo.getNotes().get(i + 1);
        double secondsForRest = nextNote.getTimestamp() - note.getTimestamp() - note.getDuration();
        noteLengthSettings = chooseNoteValues(secondsForRest);
        if (noteLengthSettings != null) {
          createRest(measure, document, noteLengthSettings.lengthDuration, noteLengthSettings.name);
        }
      }

    }
  }

  private void createRest(Element measure, Document document, Integer durationValue,
      String typeValue) {
    Element note = document.createElement("note");
    measure.appendChild(note);

    Element rest = document.createElement("rest");
    note.appendChild(rest);

    Element duration = document.createElement("duration");
    duration.appendChild(document.createTextNode(String.valueOf(durationValue)));
    note.appendChild(duration);

    Element type = document.createElement("type");
    type.appendChild(document.createTextNode(typeValue));
    note.appendChild(type);

  }

  private void crateNote(Element measure, Document document, String stepValue,
      String octaveValue, Integer durationValue, String typeValue) {

    Element note = document.createElement("note");
    measure.appendChild(note);

    Element pitch = document.createElement("pitch");
    note.appendChild(pitch);

    Element step = document.createElement("step");
    step.appendChild(document.createTextNode(stepValue));
    pitch.appendChild(step);

    Element octave = document.createElement("octave");
    octave.appendChild(document.createTextNode(octaveValue));
    pitch.appendChild(octave);

    Element duration = document.createElement("duration");
    duration.appendChild(document.createTextNode(String.valueOf(durationValue)));
    note.appendChild(duration);

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

  private Map<String, Double> getNotesLengthInSeconds(int bpm) {
    Map<String, Double> notesLenthDict = new HashMap<>();
    notesLenthDict.put("whole", (double) 2 * (60 / bpm));
    notesLenthDict.put("half", (double) (60 / bpm));
    notesLenthDict.put("quarter", (double) 0.5 * (60 / bpm));
    notesLenthDict.put("eighth", (double) 0.25 * (60 / bpm));
    notesLenthDict.put("16th", (double) 0.125 * (60 / bpm));
    return notesLenthDict;
  }

  private Map<String, Integer> getNotesLengthInDuration(int divisions) {
    Map<String, Integer> notesLenthDict = new HashMap<>();
    notesLenthDict.put("whole", divisions * 4);
    notesLenthDict.put("half", divisions * 2);
    notesLenthDict.put("quarter", divisions);
    notesLenthDict.put("eighth", divisions / 2);
    notesLenthDict.put("16th", divisions / 4);
    return notesLenthDict;
  }

  private void addTempo(Element measure, Document document, int bpm) {
    Element direction = document.createElement("direction");
    measure.appendChild(direction);
/////
    Element sound = document.createElement("sound");
    sound.setAttribute("tempo", String.valueOf(bpm));
    direction.appendChild(sound);

    measure.appendChild(direction);
  }
  /*
  <direction directive="yes" placement="above">
        <direction-type>
          <words default-y="26" font-size="11" font-weight="bold">Ziemlich langsam und mit Ausdruck</words>
        </direction-type>
        <sound tempo="60"/>
   </direction>/
      *
   */
}
