package com.soundscribe.converters.musicxml.functions;

import com.soundscribe.converters.PyinNote;
import com.soundscribe.converters.XmlPojo;
import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import com.soundscribe.converters.musicxml.entity.MusicXmlNoteTypes;
import com.soundscribe.converters.musicxml.utilities.MusicXmlNoteUtils;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.util.ArrayList;
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
public class XmlToMusicXml {

  private final SoundscribeConfiguration soundscribeConfiguration;

  public File convertXmlToMusicXml(File xml)
      throws ParserConfigurationException, TransformerException {
    XmlPojo xmlPojo = XmlPojo.readXMLData(xml);
    File musicXmlFile = new File(
        soundscribeConfiguration.getSongDataStorage() + xmlPojo.getSongName() + ".musicxml");

    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();

    Element scorePartwise = document.createElement("score-partwise");
    document.appendChild(scorePartwise);

    Element movementTitle = document.createElement("movement-title");
    movementTitle.appendChild(document.createTextNode(xmlPojo.getSongName()));
    scorePartwise.appendChild(movementTitle);

    scorePartwise.appendChild(createTitle(document, xmlPojo.getSongName()));

    Element partList = document.createElement("part-list");
    scorePartwise.appendChild(partList);

    Element scorePart = document.createElement("score-part");
    scorePart.setAttribute("id", "P1");
    partList.appendChild(scorePart);

    Element partName = document.createElement("part-name");
    partName.appendChild(document.createTextNode("Music"));
    scorePart.appendChild(partName);

    Element part = document.createElement("part");
    part.setAttribute("id", "P1");
    scorePartwise.appendChild(part);

    Element measure = document.createElement("measure");
    part.setAttribute("number", "1");
    part.appendChild(measure);

    measure.appendChild(createAttributes(document, xmlPojo.getDivisions()));
    measure.appendChild(createTempo(document, xmlPojo.getBpm()));
    addNotesToMusicXml(measure, document, xmlPojo);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(document);
    StreamResult streamResult = new StreamResult(musicXmlFile);

    transformer.transform(domSource, streamResult);
    return null;
  }

  private Element createAttributes(Document document, int divisionsValue) {
    Element attributes = document.createElement("attributes");

    Element divisions = document.createElement("divisions");
    divisions.appendChild(document.createTextNode(String.valueOf(divisionsValue)));
    attributes.appendChild(divisions);

    Element time = document.createElement("time");
    attributes.appendChild(time);

    Element beats = document.createElement("beats");
    beats.appendChild(
        document.createTextNode(String.valueOf(soundscribeConfiguration.getDefaultBeats())));
    time.appendChild(beats);

    Element beatType = document.createElement("beat-type");
    beatType.appendChild(
        document.createTextNode(String.valueOf(soundscribeConfiguration.getDefaultBeatType())));
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

  private void addNotesToMusicXml(Element measure, Document document, XmlPojo xmlPojo) {
    MusicXmlNoteUtils musicXmlNoteUtils = new MusicXmlNoteUtils();
    ArrayList<MusicXmlNote> musicXmlBaseNotes = musicXmlNoteUtils
        .getMusicXmlBaseNotes(xmlPojo.getBpm(), xmlPojo.getDivisions());
    int numberOfNotes = xmlPojo.getNotes().size();
    for (int i = 0; i < numberOfNotes; i++) {
      PyinNote note = xmlPojo.getNotes().get(i);
      String stepValue = getStep(note.getLetterNote());
      String octaveValue = getOctave(note.getLetterNote());
      MusicXmlNote musicXmlNote = musicXmlNoteUtils
          .chooseBestNoteByDurationInSeconds(note.getDurationInSeconds(), musicXmlBaseNotes, true,
              false);

      if (musicXmlNote != null) {
        Element noteElement = createNote(document, stepValue, octaveValue,
            musicXmlNote.getDuration(),
            musicXmlNote.getName(), MusicXmlNoteTypes.PITCH, musicXmlNote.isWithDot());
        measure.appendChild(noteElement);
      }

      if (i < numberOfNotes - 1) {
        PyinNote nextNote = xmlPojo.getNotes().get(i + 1);
        double secondsForRest =
            nextNote.getTimestamp() - note.getTimestamp() - note.getDurationInSeconds();
        musicXmlNote = musicXmlNoteUtils
            .chooseBestNoteByDurationInSeconds(secondsForRest, musicXmlBaseNotes, true, false);
        if (musicXmlNote != null) {
          Element noteElement = createNote(document, stepValue, octaveValue,
              musicXmlNote.getDuration(),
              musicXmlNote.getName(), MusicXmlNoteTypes.REST, musicXmlNote.isWithDot());
          measure.appendChild(noteElement);
        }
      }

    }
  }

  private Element createNote(Document document, String stepValue, String octaveValue,
      Integer durationValue, String typeValue,
      MusicXmlNoteTypes noteType, boolean withDot) {
    Element note = document.createElement("note");

    if (noteType == MusicXmlNoteTypes.REST) {
      note.appendChild(document.createElement("rest"));
    } else {
      Element pitch = document.createElement("pitch");

      Element step = document.createElement("step");
      step.appendChild(document.createTextNode(stepValue));
      pitch.appendChild(step);

      Element octave = document.createElement("octave");
      octave.appendChild(document.createTextNode(octaveValue));
      pitch.appendChild(octave);

      note.appendChild(pitch);
    }

    Element duration = document.createElement("duration");
    duration.appendChild(document.createTextNode(String.valueOf(durationValue)));
    note.appendChild(duration);

    Element type = document.createElement("type");
    type.appendChild(document.createTextNode(typeValue));
    note.appendChild(type);

    if (withDot) {
      note.appendChild(document.createElement("dot"));
    }

    return note;
  }

  private String getStep(String noteSymbol) {
    return noteSymbol.substring(0, noteSymbol.length() - 1);
  }

  private String getOctave(String noteSymbol) {
    return noteSymbol.substring(noteSymbol.length() - 1, noteSymbol.length());
  }

  private Element createTempo(Document document, int bpm) {
    Element direction = document.createElement("direction");
    direction.setAttribute("placement", "above");

    Element directionType = document.createElement("direction-type");
    direction.appendChild(directionType);

    Element metronome = document.createElement("metronome");
    directionType.appendChild(metronome);

    Element beatUnit = document.createElement("beat-unit");
    beatUnit.appendChild(document.createTextNode("quarter"));
    metronome.appendChild(beatUnit);

    Element perMinute = document.createElement("per-minute");
    perMinute.appendChild(document.createTextNode(String.valueOf(bpm)));
    metronome.appendChild(perMinute);

    Element sound = document.createElement("sound");
    sound.setAttribute("tempo", String.valueOf(bpm));
    direction.appendChild(sound);

    return direction;
  }

  private Element createTitle(Document document, String title) {
    Element credit = document.createElement("credit");
    credit.setAttribute("page", "1");

    Element creditType = document.createElement("credit-type");
    creditType.appendChild(document.createTextNode("title"));
    credit.appendChild(creditType);

    Element creditWords = document.createElement("credit-words");
    creditWords.setAttribute("justify", "center");
    creditWords.setAttribute("valign", "left");

    creditWords.appendChild(document.createTextNode(title));
    credit.appendChild(creditWords);

    return credit;
  }
}