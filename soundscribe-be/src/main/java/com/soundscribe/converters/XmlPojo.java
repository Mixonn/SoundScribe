package com.soundscribe.converters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XmlPojo is class of objects that stores data about song generated by pYIN.
 */
@Getter
@Setter
@Slf4j
public class XmlPojo {

  private String songName;
  private Integer bpm;
  private Integer divisions;
  private List<PyinNote> notes;

  /**
   * Reads data from xml file and stores it into object for next processing.
   *
   * @param fileXML Xml file with notes.
   * @return XmlPojo object with parsed data.
   */
  public static XmlPojo readXMLData(File fileXML) {
    XmlPojo xmlPojo = new XmlPojo();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      log.error("Error while converting XML.", e);
      throw new RuntimeException(e);
    }
    Document document;
    try {
      document = builder.parse(fileXML);
    } catch (SAXException e) {
      log.error("Invalid xml file format", e);
      throw new RuntimeException(e);
    } catch (IOException e) {
      log.error("Could not find xml file.", e);
      throw new RuntimeException(e);
    }
    document.getDocumentElement().normalize();

    Element root = document.getDocumentElement();
    String songName = root.getNodeName();
    xmlPojo.setSongName(songName);

    Element soundElement = (Element) document.getElementsByTagName("bpm").item(0);
    xmlPojo.setBpm(Integer.parseInt(soundElement.getTextContent()));

    Element divisionsElement = (Element) document.getElementsByTagName("divisions").item(0);
    xmlPojo.setDivisions(Integer.parseInt(divisionsElement.getTextContent()));

    List<PyinNote> noteList = new ArrayList<>();
    NodeList nList = document.getElementsByTagName("note");
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node node = nList.item(temp);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) node;

        double timestamp =
            Double.parseDouble(eElement.getElementsByTagName("timestamp").item(0).getTextContent());
        double duration =
            Double.parseDouble(eElement.getElementsByTagName("duration").item(0).getTextContent());
        double value =
            Double.parseDouble(eElement.getElementsByTagName("value").item(0).getTextContent());
        int midiValue =
            Integer.parseInt(eElement.getElementsByTagName("midiValue").item(0).getTextContent());
        String letterNotde = eElement.getElementsByTagName("letterNote").item(0).getTextContent();
        noteList.add(new PyinNote(timestamp, duration, value, midiValue, letterNotde));
      }
    }
    xmlPojo.setNotes(noteList);
    return xmlPojo;
  }
}
