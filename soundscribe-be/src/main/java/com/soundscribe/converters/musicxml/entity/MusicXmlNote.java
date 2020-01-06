package com.soundscribe.converters.musicxml.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/** Stores data about MusicXml notes. */
public class MusicXmlNote {

  private String name;
  private double seconds;
  private int duration;
  private boolean withDot;
}
