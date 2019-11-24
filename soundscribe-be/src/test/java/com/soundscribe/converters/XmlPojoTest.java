package com.soundscribe.converters;

import static org.junit.jupiter.api.Assertions.*;

import com.soundscribe.converters.musicxml.functions.MusicXmlToMidi;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class XmlPojoTest {

  @Test
  void readXMLData() {
    String fileContent = "<test>\n"
        + "<bpm>130</bpm>\n"
        + "<divisions>4</divisions>\n"
        + "<note>\n"
        + "<timestamp>2.65</timestamp>\n"
        + "<duration>0.21</duration>\n"
        + "<value>287.33914</value>\n"
        + "<midiValue>62</midiValue>\n"
        + "<letterNote>D4</letterNote>\n"
        + "</note>\n"
        + "</test>";
    List<String> lines = Collections.singletonList(fileContent);
    Path filePath = Paths.get("testTmp.xml");
    try {
      Files.write(filePath, lines, StandardCharsets.UTF_8);
      File xml = new File(filePath.toUri());
      XmlPojo xmlPojo = XmlPojo.readXMLData(xml);
      assertEquals(130, xmlPojo.getBpm().intValue());
      assertEquals(4, xmlPojo.getDivisions().intValue());
      assertEquals(1, xmlPojo.getNotes().size());
      assertEquals(2.65, xmlPojo.getNotes().get(0).getTimestamp());
      assertEquals(0.21, xmlPojo.getNotes().get(0).getDurationInSeconds());
      assertEquals(287.33914, xmlPojo.getNotes().get(0).getFrequency());
      assertEquals(62, xmlPojo.getNotes().get(0).getMidiValue());
      assertEquals("D4", xmlPojo.getNotes().get(0).getLetterNote());
      Files.deleteIfExists(xml.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}