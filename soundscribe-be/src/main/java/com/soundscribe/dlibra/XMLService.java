package com.soundscribe.dlibra;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Slf4j
@Service
public class XMLService {
  public static List<PublicationInfo> parseListResponse() {
    List<PublicationInfo> publications = new ArrayList<>();
    try {
      String URL =
          "https://demo.dl.psnc.pl/dlibra/oai-pmh-repository.xml?verb=ListRecords&metadataPrefix=dlibra_avs&set=dLibraDigitalLibrary:test:ss";

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(URL);

      doc.getDocumentElement().normalize();
      NodeList nodeList = doc.getElementsByTagName("record");
      for (int i = 0; i < nodeList.getLength(); i++) {
        PublicationInfo publicationInfo = new PublicationInfo();
        Element element = (Element) nodeList.item(i);

        Node idNode = element.getElementsByTagName("identifier").item(0);
        String idString = idNode.getTextContent();
        String[] splitId = idString.split(":");
        int id = Integer.parseInt(splitId[splitId.length - 1]);
        publicationInfo.setId(id);

        Element nameElement = (Element) element.getElementsByTagName("dlibra_avs:Title").item(0);
        NodeList valueNodeList = nameElement.getElementsByTagName("value");
        if (valueNodeList.getLength() != 0) {
          Node valueNode = nameElement.getElementsByTagName("value").item(0);
          String name = valueNode.getTextContent();
          publicationInfo.setTitle(name);
        } else {
          publicationInfo.setTitle("Brak tytułu");
        }
        publications.add(publicationInfo);
      }
    } catch (Exception e) {
      log.error("Wystąpił błąd podczas pobierania kolekcji SoundScribe", e);
      throw new RuntimeException("Wystąpił błąd podczas pobierania kolekcji SoundScribe", e);
    }

    return publications;
  }
}
