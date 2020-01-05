package com.soundscribe.converters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Stores detailed data about notes. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PyinNote {

  private double timestamp;
  private double durationInSeconds;
  private double frequency;
  private int midiValue;
  private String letterNote;
}
