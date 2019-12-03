package com.soundscribe.converters;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ConversionFormat {
  private String fromFormat;
  private String toFormat;
}
