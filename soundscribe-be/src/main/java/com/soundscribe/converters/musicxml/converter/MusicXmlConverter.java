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

  /**
   * Converts raw pYIN algorithm notes to MusicXml format.
   * @param xml Xml file with pYIN notes, tempo and divisions.
   * @return MusicXml file.
   */
  public File convertXmlToMusicXml(File xml) {
    File musicxml;
    try {
      musicxml = xmlToMusicXml.convertXmlToMusicXml(xml);
    } catch (ParserConfigurationException | TransformerException e) {
      log.error("Unable to create convert xml to musicxml", e);
      throw new RuntimeException(e);
    }
    return musicxml;
  }

  /**
   * Converts MusicXml file to playable Midi file.
   * @param musicXml MusicXml file.
   * @return Midi file.
   */
  public File convertMusicXmlToMidi(File musicXml){
    return musicXmlToMidi.convertMusicXmlToMidi(musicXml);
  }
}
