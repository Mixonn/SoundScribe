package com.soundscribe.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores all possible notes. If midi value out of range returns information how far it is from
 * original scales. For example. +2(too high), -3(too low).
 */
public class MidiNotes {
  private static final Map<Integer, String> notesDict;

  static {
    notesDict =
        new HashMap<>() {
          {
            put(127, "G9");
            put(126, "F#9");
            put(125, "F9");
            put(124, "E9");
            put(123, "D#9");
            put(122, "D9");
            put(121, "C#9");
            put(120, "C9");
            put(119, "B8");
            put(118, "A#8");
            put(117, "A8");
            put(116, "G#8");
            put(115, "G8");
            put(114, "F#8");
            put(113, "F8");
            put(112, "E8");
            put(111, "D#8");
            put(110, "D8");
            put(109, "C#8");
            put(108, "C8");
            put(107, "B7");
            put(106, "A#7");
            put(105, "A7");
            put(104, "G#7");
            put(103, "G7");
            put(102, "F#7");
            put(101, "F7");
            put(100, "E7");
            put(99, "D#7");
            put(98, "D7");
            put(97, "C#7");
            put(96, "C7");
            put(95, "B6");
            put(94, "A#6");
            put(93, "A6");
            put(92, "G#6");
            put(91, "G6");
            put(90, "F#6");
            put(89, "F6");
            put(88, "E6");
            put(87, "D#6");
            put(86, "D6");
            put(85, "C#6");
            put(84, "C6");
            put(83, "B5");
            put(82, "A#5");
            put(81, "A5");
            put(80, "G#5");
            put(79, "G5");
            put(78, "F#5");
            put(77, "F5");
            put(76, "E5");
            put(75, "D#5");
            put(74, "D5");
            put(73, "C#5");
            put(72, "C5");
            put(71, "B4");
            put(70, "A#4");
            put(69, "A4");
            put(68, "G#4");
            put(67, "G4");
            put(66, "F#4");
            put(65, "F4");
            put(64, "E4");
            put(63, "D#4");
            put(62, "D4");
            put(61, "C#4");
            put(60, "C4");
            put(59, "B3");
            put(58, "A#3");
            put(57, "A3");
            put(56, "G#3");
            put(55, "G3");
            put(54, "F#3");
            put(53, "F3");
            put(52, "E3");
            put(51, "D#3");
            put(50, "D3");
            put(49, "C#3");
            put(48, "C3");
            put(47, "B2");
            put(46, "A#2");
            put(45, "A2");
            put(44, "G#2");
            put(43, "G2");
            put(42, "F#2");
            put(41, "F2");
            put(40, "E2");
            put(39, "D#2");
            put(38, "D2");
            put(37, "C#2");
            put(36, "C2");
            put(35, "B1");
            put(34, "A#1");
            put(33, "A1");
            put(32, "G#1");
            put(31, "G1");
            put(30, "F#1");
            put(29, "F1");
            put(28, "E1");
            put(27, "D#1");
            put(26, "D1");
            put(25, "C#1");
            put(24, "C1");
            put(23, "B0");
            put(22, "A#0");
            put(21, "A0");
          }
        };
  }

  public static String note(int value) {
    if (value < 21) return String.valueOf(value - 21);
    else if (value > 127) return "+" + (value - 127);
    else return notesDict.get(value);
  }
}
