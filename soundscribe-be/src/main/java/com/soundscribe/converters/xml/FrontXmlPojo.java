package com.soundscribe.converters.xml;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soundscribe.converters.PyinNote;
import com.soundscribe.utilities.MidiNotes;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrontXmlPojo {
  @JsonProperty("song_name")
  private String songName;

  @JsonProperty("bpm")
  private Integer bpm;

  @JsonProperty("divisions")
  private Integer divisions;

  @JsonProperty("midi_elements")
  private List<FrontMidiPair> midiElementsList;

  public static XmlPojo convertFrontXmlPojoToXmlPojo(FrontXmlPojo frontXmlPojo) {
    XmlPojo xmlPojo = new XmlPojo();
    xmlPojo.setBpm(frontXmlPojo.getBpm());
    xmlPojo.setDivisions(frontXmlPojo.getDivisions());
    xmlPojo.setSongName(frontXmlPojo.getSongName());

    List<PyinNote> notes = new ArrayList<>();
    for (FrontMidiPair pair : frontXmlPojo.getMidiElementsList()) {
      PyinNote pyinNote = new PyinNote();
      FrontMidiEvent beginEvent = pair.getBegin();
      FrontMidiEvent endEvent = pair.getEnd();

      pyinNote.setMidiValue(beginEvent.getMidiValue());
      pyinNote.setDurationInSeconds(endEvent.getTimestamp() - beginEvent.getTimestamp());
      pyinNote.setTimestamp(beginEvent.getTimestamp());
      pyinNote.setLetterNote(MidiNotes.getNoteSymbolByMidiValue(beginEvent.getMidiValue()));
      notes.add(pyinNote);
    }
    xmlPojo.setNotes(notes);
    return xmlPojo;
  }
}
