package com.soundscribe.jvamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PyinConversionException extends Exception {
  String message;
}
