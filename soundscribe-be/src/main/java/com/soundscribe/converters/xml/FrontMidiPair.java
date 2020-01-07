package com.soundscribe.converters.xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class FrontMidiPair {
  private FrontMidiEvent begin;
  private FrontMidiEvent end;
}
