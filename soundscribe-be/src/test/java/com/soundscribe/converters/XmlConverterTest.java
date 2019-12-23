package com.soundscribe.converters;

import com.soundscribe.utilities.SoundscribeConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
class XmlConverterTest {

  private File xmlFile;
  private File midiFile;


  @BeforeAll
  void setUp() {
    ClassLoader classLoader = getClass().getClassLoader();
    xmlFile = new File(classLoader.getResource("samples/TP0300A_04.xml").getFile());
  }

  @AfterAll
  void tearDown() {
    if (xmlFile != null) {
      try {
        Files.delete(xmlFile.toPath());
      } catch (IOException ignored) {
      }
    }
    if (midiFile != null) {
      try {
        Files.delete(midiFile.toPath());
      } catch (IOException ignored) {
      }
    }
  }

  @Test
  void testConvertXmlToMidi() {
    XmlConverter xmlConverter = new XmlConverter(new SoundscribeConfiguration());
      midiFile = xmlConverter.convertXmlToMidi(xmlFile);
    assertTrue(midiFile.exists());
  }
}
