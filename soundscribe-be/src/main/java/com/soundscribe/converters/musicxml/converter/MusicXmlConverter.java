package com.soundscribe.converters.musicxml.converter;

import com.soundscribe.converters.musicxml.functions.MusicXmlToMidi;
import com.soundscribe.converters.musicxml.functions.XmlToMusicXml;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusicXmlConverter {

  private final XmlToMusicXml xmlToMusicXml;
  private final MusicXmlToMidi musicXmlToMidi;

  public File convertXmlToMusicXml(File xml) {
    File musicxml = null;
    try {
      musicxml = xmlToMusicXml.convertXmlToMusicXml(xml);
    } catch (ParserConfigurationException | TransformerException e) {
      log.error("Unable to create convert xml to musicxml", e);
      throw new RuntimeException(e);
    }
    return musicxml;
  }

  public File convertMusicXmlToMidi(File musicXml) {
    return musicXmlToMidi.convertMusicXmlToMidi(musicXml);
  }
}
