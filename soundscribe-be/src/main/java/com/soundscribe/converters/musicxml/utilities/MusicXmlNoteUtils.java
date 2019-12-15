package com.soundscribe.converters.musicxml.utilities;

import com.soundscribe.converters.musicxml.entity.MusicXmlNote;
import com.soundscribe.utilities.MusicXmlConfiguration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** MusicXmlNoteUtils contains common utilities for other MusicXml functions. */
@RequiredArgsConstructor
@Component
public class MusicXmlNoteUtils {

  private final MusicXmlConfiguration musicXmlConfiguration;
  /**
   * Based on bpm and number of divisions per quarter note, function calculates necessary
   * information about notes in MusicXMl
   *
   * @param bpm song tempo
   * @param divisions divisions per quarter note
   * @return List of note with standard length
   */
  public List<MusicXmlNote> getMusicXmlBaseNotes(int bpm, int divisions) {
    double quarterNoteTime = (double) 60 / bpm;
    ArrayList<MusicXmlNote> notesLengthList = new ArrayList<>();
    notesLengthList.add(new MusicXmlNote("whole", quarterNoteTime * 4, divisions * 4, false));
    notesLengthList.add(new MusicXmlNote("half", quarterNoteTime * 2, divisions * 2, false));
    notesLengthList.add(new MusicXmlNote("quarter", quarterNoteTime, divisions, false));
    notesLengthList.add(new MusicXmlNote("eighth", quarterNoteTime / 2, divisions / 2, false));
    notesLengthList.add(new MusicXmlNote("16th", quarterNoteTime / 4, divisions / 4, false));

    if (musicXmlConfiguration.isUseDots()) {
      notesLengthList.add(
          new MusicXmlNote("whole", quarterNoteTime * 4 * 1.5, (int) (divisions * 4 * 1.5), true));
      notesLengthList.add(
          new MusicXmlNote("half", quarterNoteTime * 2 * 1.5, (int) (divisions * 2 * 1.5), true));
      notesLengthList.add(
          new MusicXmlNote("quarter", quarterNoteTime * 1.5, (int) (divisions * 1.5), true));
      notesLengthList.add(
          new MusicXmlNote("eighth", quarterNoteTime / 2 * 1.5, (int) (divisions / 2 * 1.5), true));
      notesLengthList.add(
          new MusicXmlNote("16th", quarterNoteTime / 4 * 1.5, (int) (divisions / 4 * 1.5), true));
    }

    return notesLengthList;
  }

  /**
   * Based on given sound time, and calculated base notes function returns the best-matching
   * element. Thanks to flags delete16th and force16th you can choose if you want to add or delete
   * very short notes.
   *
   * @param durationInSeconds Duration of sound in seconds.
   * @param baseNotes List of calculated for adequate bpm notes.
   * @param isRest
   * @return Best-matching note.
   */
  public MusicXmlNote chooseBestNoteByDurationInSeconds(
      double durationInSeconds, List<MusicXmlNote> baseNotes, boolean isRest) {
    MusicXmlNote bestFittingNote = null;
    double bestDiffInSeconds = 0;
    for (MusicXmlNote musicXmlNote : baseNotes) {
      double diffInSeconds;
      if (bestFittingNote == null) {
        bestFittingNote = musicXmlNote;
        bestDiffInSeconds = Math.abs(durationInSeconds - musicXmlNote.getSeconds());
      } else {
        diffInSeconds = Math.abs(durationInSeconds - musicXmlNote.getSeconds());
        if (diffInSeconds < bestDiffInSeconds) {
          bestDiffInSeconds = diffInSeconds;
          bestFittingNote = musicXmlNote;
        }
      }
    }

    if (bestFittingNote != null && bestFittingNote.getName().equals("16th")) {
      if (isRest && musicXmlConfiguration.isDelete16thRest()) {
        return null;
      } else if (!isRest && musicXmlConfiguration.isDelete16thPitch()) {
        return null;
      }
    }

    return bestFittingNote;
  }

  /**
   * Returns time in seconds for MusicXml note.
   *
   * @param duration Relative length of musicXml note.
   * @param baseNotes List of calculated for adequate bpm notes.
   * @return
   */
  public double getNoteDurationInSeconds(int duration, List<MusicXmlNote> baseNotes) {
    for (MusicXmlNote note : baseNotes) {
      if (note.getDuration() == duration) {
        return note.getSeconds();
      }
    }
    return 0;
  }
}
