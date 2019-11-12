package com.soundscribe.converters;

import com.soundscribe.core.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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

import static org.junit.Assert.assertTrue;

/* TODO: preferably move to JUnit5 */

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class MidiConverterTest {

  private File xmlFile;
  private File midiFile;
  @Autowired
  private MidiConverter midiConverter;

  @Before
  public void setUp() {
    String testFileName = "testName";
    xmlFile = new File(testFileName + ".xml");
    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = null;
    try {
      documentBuilder = documentFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    Document document = documentBuilder.newDocument();

    Element root = document.createElement(testFileName);
    document.appendChild(root);

    for (int i = 1; i < 3; i++) {
      Element note = document.createElement("note");

      double timestampValue = 1.11 * i;
      Element timestamp = document.createElement("timestamp");
      timestamp.appendChild(document.createTextNode(String.valueOf(timestampValue)));
      note.appendChild(timestamp);

      double durationValue = 0.5 * i;
      Element duration = document.createElement("duration");
      duration.appendChild(document.createTextNode(String.valueOf(durationValue)));
      note.appendChild(duration);

      double frequencyValue = 262;
      Element val = document.createElement("value");
      val.appendChild(document.createTextNode(String.valueOf(frequencyValue)));
      note.appendChild(val);

      int midiValue = 60;
      Element midi = document.createElement("midiValue");
      midi.appendChild(document.createTextNode(String.valueOf(midiValue)));
      note.appendChild(midi);

      String midiNote = "C4";
      Element letterNote = document.createElement("letterNote");
      letterNote.appendChild(document.createTextNode(midiNote));
      note.appendChild(letterNote);

      root.appendChild(note);
    }
    // create the xml file
    // transform the DOM Object to an XML File
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = transformerFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    }
    DOMSource domSource = new DOMSource(document);
    StreamResult streamResult = new StreamResult(xmlFile);

    try {
      transformer.transform(domSource, streamResult);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }

  @After
  public void tearDown() {
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
  public void convertXMLtoMidiTestIfFileCreated() {
    midiFile = midiConverter.convertXmlToMidi(xmlFile);
    assertTrue(midiFile.exists());
  }
}