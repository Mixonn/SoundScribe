package com.soundscribe.converters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores detailed data about notes.
 */
@Getter
@Setter
@AllArgsConstructor
public class NotePojo {

  private double timestamp;
  private double durationInSeconds;
  private double frequency;
  private int midiValue;
  private String letterNote;
}
