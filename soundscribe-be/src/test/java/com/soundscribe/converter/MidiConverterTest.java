package com.soundscribe.converter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

public class MidiConverterTest {

  private File xmlFile = null;
  private File midiFile = null;
  private MidiConverter midiConverter = null;
  private String testFileName = "testName";

  @Before
  public void setUp() {
    xmlFile = new File(testFileName + ".xml");
    midiConverter = new MidiConverter();

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
    if (xmlFile != null) xmlFile.delete();
    if (midiFile != null) midiFile.delete();
  }

  @Test
  public void convertXMLtoMidiTestIfFileCreated() {
    midiFile = midiConverter.convertXmlToMidi(xmlFile);
    assertTrue(midiFile.exists());
  }
}
