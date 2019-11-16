package com.soundscribe.converters.musicxml.functions;

import static org.junit.jupiter.api.Assertions.*;

import com.soundscribe.converters.MidiConverter;
import com.soundscribe.AppConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = AppConfig.class)
class MusicXmlToMidiTest {

  @Autowired
  private MidiConverter midiConverter;
  private String musicXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
      + "<score-partwise>\n"
      + "  <movement-title>test</movement-title>\n"
      + "  <credit page=\"1\">\n"
      + "    <credit-type>title</credit-type>\n"
      + "    <credit-words justify=\"center\" valign=\"left\">test</credit-words>\n"
      + "  </credit>\n"
      + "  <part-list>\n"
      + "    <score-part id=\"P1\">\n"
      + "      <part-name>Music</part-name>\n"
      + "    </score-part>\n"
      + "  </part-list>\n"
      + "  <part id=\"P1\" number=\"1\">\n"
      + "    <measure>\n"
      + "      <attributes>\n"
      + "        <divisions>4</divisions>\n"
      + "        <time>\n"
      + "          <beats>4</beats>\n"
      + "          <beat-type>4</beat-type>\n"
      + "        </time>\n"
      + "        <clef>\n"
      + "          <sign>G</sign>\n"
      + "          <line>2</line>\n"
      + "        </clef>\n"
      + "      </attributes>\n"
      + "      <direction placement=\"above\">\n"
      + "        <direction-type>\n"
      + "          <metronome>\n"
      + "            <beat-unit>quarter</beat-unit>\n"
      + "            <per-minute>130</per-minute>\n"
      + "          </metronome>\n"
      + "        </direction-type>\n"
      + "        <sound tempo=\"130\"/>\n"
      + "      </direction>\n"
      + "      <note>\n"
      + "        <rest/>\n"
      + "        <duration>24</duration>\n"
      + "        <type>whole</type>\n"
      + "        <dot/>\n"
      + "      </note>\n"
      + "      <note>\n"
      + "        <pitch>\n"
      + "          <step>D</step>\n"
      + "          <octave>4</octave>\n"
      + "        </pitch>\n"
      + "        <duration>2</duration>\n"
      + "        <type>eighth</type>\n"
      + "      </note>\n"
      + "      <note>\n"
      + "        <pitch>\n"
      + "          <step>E</step>\n"
      + "          <octave>4</octave>\n"
      + "        </pitch>\n"
      + "        <duration>2</duration>\n"
      + "        <type>eighth</type>\n"
      + "      </note>\n"
      + "    </measure>\n"
      + "  </part>\n"
      + "</score-partwise>\n";

  private File musicXmlFile;
  private File midiFile;

  @BeforeAll
  void init() {
    List<String> lines = Collections.singletonList(musicXmlContent);
    Path filePath = Paths.get("test.musicxml");
    try {
      musicXmlFile = new File(filePath.toString());
      Files.write(filePath, lines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void convertMusicXmlToMidi() {
    MusicXmlToMidi musicXmlToMidi = new MusicXmlToMidi(midiConverter);
    musicXmlToMidi.convertMusicXmlToMidi(musicXmlFile);

    String pathToMidi = "test.mid";
    midiFile = new File(pathToMidi);
    assertTrue(midiFile.exists());
  }

  @AfterAll
  void clean(){
    try {
      Files.deleteIfExists(musicXmlFile.toPath());
      Files.deleteIfExists(midiFile.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}