package com.soundscribe.converters.musicxml.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicXmlNote {

  private String name;
  private double seconds;
  private int duration;
  private boolean withDot;
}
