package com.soundscribe.converters.musicxml.utilities;

import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import java.util.ArrayList;

/**
 * MusicXmlNoteUtils contains common utilities for other MusicXml functions.
 */
public class MusicXmlNoteUtils {

  /**
   * Based on bpm and number of divisions per quarter note, function calculates necessary information about notes in MusicXMl
   * @param bpm song tempo
   * @param divisions divisions per quarter note
   * @return List of note with standard length
   */
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

  /**
   * Based on given sound time, and calculated base notes function returns the best-matching element.
   * Thanks to flags delete16th and force16th you can choose if you want to add or delete very short notes.
   * @param durationInSeconds Duration of sound in seconds.
   * @param baseNotes List of calculated for adequate bpm notes.
   * @param delete16th True if you dont want to add 16th notes to musicXml.
   * @param force16th True if you want to add very short notes detected by pYIN.
   * @return Best-matching note.
   */
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

  /**
   * Returns time in seconds for MusicXml note.
   * @param duration Relative length of musicXml note.
   * @param baseNotes List of calculated for adequate bpm notes.
   * @return
   */
  public double getNoteDurationInSeconds(int duration,ArrayList<MusicXmlNote> baseNotes){
    for(MusicXmlNote note :baseNotes){
      if(note.getDuration()==duration){
        return note.getSeconds();
      }
    }
    return 0;
  }
}
