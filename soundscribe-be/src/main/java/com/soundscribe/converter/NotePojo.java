package com.soundscribe.converter;

import lombok.Data;

/**
 * Stores detailed data about notes.
 */
@Data
public class NotePojo {

  private double timestamp;
  private double duration;
  private double value;
  private int midiValue;
  private String letterNote;

  public NotePojo(double timestamp, double duration, double value, int midiValue,
      String letterNote) {
    this.timestamp = timestamp;
    this.duration = duration;
    this.value = value;
    this.midiValue = midiValue;
    this.letterNote = letterNote;
  }

}
