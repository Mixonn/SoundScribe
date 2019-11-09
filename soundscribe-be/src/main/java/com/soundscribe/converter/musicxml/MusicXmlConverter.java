package com.soundscribe.converter.musicxml;

import com.soundscribe.converter.NotePojo;
import com.soundscribe.converter.XmlPojo;
import com.soundscribe.converter.musicxml.MusicXmlNoteTypes;
import com.soundscribe.converter.musicxml.MusicXmlNote;
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
public class MusicXmlConverter {

  private final SoundscribeConfiguration soundscribeConfiguration;
  private int bpm = 150;
  private int divisionsValue = 4;
  private int beatsValue = 4;
  private int beatTypeValue = 4;
  private ArrayList<MusicXmlNote> musicXmlBaseNotes = null;

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

    measure.appendChild(createAttributes(document));
    addTempo(measure, document, bpm);
    addNotesToMusicXml(measure, document, xmlPojo);

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

  private MusicXmlNote chooseBestNote(double durationInSeconds, boolean delete16th,
      boolean force16th) {
    for (MusicXmlNote musicXmlNote : musicXmlBaseNotes) {
      if (durationInSeconds > musicXmlNote.getSeconds() / 1.25) {
        if (delete16th && musicXmlNote.getName().equals("16th")) {
          return null;
        } else {
          return musicXmlNote;
        }
      }
    }
    if (force16th) {
      return musicXmlBaseNotes.get(musicXmlBaseNotes.size() - 1);
    } else {
      return null;
    }
  }

  private void addNotesToMusicXml(Element measure, Document document, XmlPojo xmlPojo) {
    musicXmlBaseNotes = getMusicXmlBaseNotes(bpm, divisionsValue);
    int numberOfNotes = xmlPojo.getNotes().size();
    for (int i = 0; i < numberOfNotes; i++) {
      NotePojo note = xmlPojo.getNotes().get(i);
      String stepValue = getStep(note.getLetterNote());
      String octaveValue = getOctave(note.getLetterNote());
      MusicXmlNote musicXmlNote = chooseBestNote(note.getDuration(), true, false);

      if (musicXmlNote != null) {
        Element noteElement = createNote(document, stepValue, octaveValue,
            musicXmlNote.getDuration(),
            musicXmlNote.getName(), MusicXmlNoteTypes.PITCH, musicXmlNote.isWithDot());
        measure.appendChild(noteElement);
      }

      if (i < numberOfNotes - 1) {
        NotePojo nextNote = xmlPojo.getNotes().get(i + 1);
        double secondsForRest = nextNote.getTimestamp() - note.getTimestamp() - note.getDuration();
        musicXmlNote = chooseBestNote(secondsForRest, true, false);
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

  private ArrayList<MusicXmlNote> getMusicXmlBaseNotes(int bpm, int divisions) {
    double quarterNoteTime = (double) 60 / bpm;
    ArrayList<MusicXmlNote> notesLengthList = new ArrayList<>();
    notesLengthList
        .add(new MusicXmlNote("whole", quarterNoteTime * 4 * 1.5, (int) (divisions * 4 * 1.5),
            true));
    notesLengthList.add(new MusicXmlNote("whole", quarterNoteTime * 4, divisions * 4, false));
    notesLengthList
        .add(new MusicXmlNote("half", quarterNoteTime * 2 * 1.5, (int) (divisions * 2 * 1.5),
            true));
    notesLengthList.add(new MusicXmlNote("half", quarterNoteTime * 2, divisions * 2, false));
    notesLengthList
        .add(new MusicXmlNote("quarter", quarterNoteTime * 1.5, (int) (divisions * 1.5), true));
    notesLengthList.add(new MusicXmlNote("quarter", quarterNoteTime, divisions, false));
    notesLengthList
        .add(new MusicXmlNote("eighth", quarterNoteTime / 2 * 1.5, (int) (divisions / 2 * 1.5),
            true));
    notesLengthList.add(new MusicXmlNote("eighth", quarterNoteTime / 2, divisions / 2, false));
    notesLengthList
        .add(new MusicXmlNote("16th", quarterNoteTime / 4 * 1.5, (int) (divisions / 4 * 1.5),
            true));
    notesLengthList.add(new MusicXmlNote("16th", quarterNoteTime / 4, divisions / 4, false));
    return notesLengthList;
  }

  private void addTempo(Element measure, Document document, int bpm) {
    Element direction = document.createElement("direction");
    measure.appendChild(direction);

    Element sound = document.createElement("sound");
    sound.setAttribute("tempo", String.valueOf(bpm));
    direction.appendChild(sound);

    measure.appendChild(direction);
  }
}
