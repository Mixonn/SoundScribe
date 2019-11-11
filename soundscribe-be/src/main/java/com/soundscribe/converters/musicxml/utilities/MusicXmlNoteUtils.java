package com.soundscribe.converters.musicxml.utilities;

import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import java.util.ArrayList;

public class MusicXmlNoteUtils {
  public ArrayList<MusicXmlNote> getMusicXmlBaseNotes(int bpm, int divisions) {
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

  public MusicXmlNote chooseBestNoteByDurationInSeconds(double durationInSeconds,ArrayList<MusicXmlNote> baseNotes, boolean delete16th, boolean force16th) {
    for (MusicXmlNote musicXmlNote : baseNotes) {
      if (durationInSeconds > musicXmlNote.getSeconds() / 1.25) {
        if (delete16th && musicXmlNote.getName().equals("16th")) {
          return null;
        } else {
          return musicXmlNote;
        }
      }
    }
    if (force16th) {
      return baseNotes.get(baseNotes.size() - 1);
    } else {
      return null;
    }
  }

  public double getNoteDurationInSeconds(int duration,ArrayList<MusicXmlNote> baseNotes){
    for(MusicXmlNote note :baseNotes){
      if(note.getDuration()==duration){
        return note.getSeconds();
      }
    }
    return 0;
  }
}
