package com.soundscribe.jvamp;

import com.soundscribe.utilities.MidiNotes;
import com.soundscribe.utilities.SoundscribeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vamp_plugins.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Host is slightly modified jVAMP class. It provides support for loading VAMP plugins.
 * Source: https://github.com/c4dm/jvamp
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Host {

  private final SoundscribeConfiguration soundscribeConfiguration;

  public File start(JvampFunctions function, File file) throws PyinConversionException {
    File xmlFile = null;
    File smoothedFile = null;
    File tempoFile = null;
    String key;
    PluginLoader loader = PluginLoader.getInstance();
    String fileName = file.getName().split("\\.")[0];
    if (function == JvampFunctions.NOTES) {
      key = "pyin:pyin:notes";
      xmlFile = new File(soundscribeConfiguration.getSongDataStorage() + fileName + ".xml");
    } else if (function == JvampFunctions.SMOOTHED_PITCH_TRACK) {
      key = "pyin:pyin:smoothedpitchtrack";
      smoothedFile = new File(soundscribeConfiguration.getSongDataStorage() + fileName + ".txt");
    } else {
      // key = "vamp-aubio:aubiotempo:tempo";
      key = "vamp-example-plugins:fixedtempo:tempo";
      tempoFile = new File(soundscribeConfiguration.getSongDataStorage() + fileName + "_tempo.txt");
    }

    String[] keyparts = key.split(":");

    String pluginKey = keyparts[0] + ":" + keyparts[1];
    String outputKey = keyparts[2];

    try {
      AudioInputStream stream = AudioSystem.getAudioInputStream(file);
      AudioFormat format = stream.getFormat();

      if (format.getSampleSizeInBits() != 16
              || format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
              || format.isBigEndian()) {
        log.error("Sorry, only 16-bit signed little-endian PCM files supported");
        throw new PyinConversionException(
                "Sorry, only 16-bit signed little-endian PCM files supported");
      }

      float rate = format.getFrameRate();
      int channels = format.getChannels();
      int blockSize = 1024; // frames

      Plugin p = loader.loadPlugin(pluginKey, rate, PluginLoader.AdapterFlags.ADAPT_ALL);

      OutputDescriptor[] outputs = p.getOutputDescriptors();
      int outputNumber = -1;
      for (int i = 0; i < outputs.length; ++i) {
        if (outputs[i].identifier.equals(outputKey)) {
          outputNumber = i;
        }
      }
      if (outputNumber < 0) {
        log.error("Plugin lacks output id: " + outputKey);
        log.error("Outputs are:");
        for (OutputDescriptor output : outputs) {
          log.error(" " + output.identifier);
        }
        throw new PyinConversionException("Invalid number of keys");
      }

      boolean b = p.initialise(channels, blockSize, blockSize);
      if (!b) {
        log.error("Plugin initialise failed");
        throw new PyinConversionException("Plugin initialise failed");
      }

      float[][] buffers = new float[channels][blockSize];

      boolean done = false;
      boolean incomplete = false;
      int block = 0;

      Map<Integer, List<Feature>> actualFeatures = null;

      while (!done) {

        for (int c = 0; c < channels; ++c) {
          for (int i = 0; i < blockSize; ++i) {
            buffers[c][i] = 0.0f;
          }
        }

        int read = readBlock(format, stream, buffers);

        if (read < 0) {
          done = true;
        } else {

          if (incomplete) {
            // An incomplete block is only OK if it's the
            // last one -- so if the previous block was
            // incomplete, we have trouble
            log.error("Audio file read incomplete! Short buffer detected at " + block * blockSize);
            throw new PyinConversionException(
                    "Audio file read incomplete! Short buffer detected at " + block * blockSize);
          }

          incomplete = (read < buffers[0].length);

          RealTime timestamp = RealTime.frame2RealTime(block * blockSize, (int) (rate + 0.5));

          Map<Integer, List<Feature>> features = p.process(buffers, timestamp);

          if (function == JvampFunctions.SIMPLE_FIXED_TEMPO_ESTIMATOR
                  && features != null
                  && features.containsKey(outputNumber)) {
            actualFeatures = features;
          }

          p.process(buffers, timestamp);
        }

        ++block;
      }

      Map<Integer, List<Feature>> features = p.getRemainingFeatures();

      RealTime timestamp = RealTime.frame2RealTime(block * blockSize, (int) (rate + 0.5));
      if (function == JvampFunctions.NOTES) {
        printNotes(fileName, timestamp, outputNumber, features, xmlFile);
      } else if (function == JvampFunctions.SMOOTHED_PITCH_TRACK) {
        printSmoothedPitch(timestamp, outputNumber, features, smoothedFile);
      } else {
        printTempo(actualFeatures, outputNumber, tempoFile);
      }

      p.dispose();
    } catch (IOException e) {
      log.error("Failed to read audio file: " + e.getMessage());

    } catch (UnsupportedAudioFileException e) {
      log.error("Unsupported audio file format: " + e.getMessage());

    } catch (PluginLoader.LoadFailedException e) {
      log.error("Plugin load failed (unknown plugin?): key is " + key);
    }

    if (function == JvampFunctions.NOTES) {
      return xmlFile;
    } else if (function == JvampFunctions.SMOOTHED_PITCH_TRACK) {
      return smoothedFile;
    } else {
      return tempoFile;
    }
  }

  /**
   * Generates xml file from data calculated by pYIN algorithm. Additionaly it adds letterNote
   * attribute which
   *
   * @param filename Song name
   * @param frameTime
   * @param output
   * @param features
   * @param xmlFile Xml file with pYIN data
   */
  private void printNotes(
          String filename,
          RealTime frameTime,
          Integer output,
          Map<Integer, List<Feature>> features,
          File xmlFile) {
    int midiValue;
    if (!features.containsKey(output)) {
      return;
    }
    File wavFile = new File(soundscribeConfiguration.getSongDataStorage() + filename + ".wav");

    try {
      DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();

      Element root = document.createElement(filename);
      document.appendChild(root);

      Element bpm = document.createElement("bpm");
      bpm.appendChild(document.createTextNode(estimateTempo(wavFile, false)));
      root.appendChild(bpm);

      Element divisions = document.createElement("divisions");
      divisions.appendChild(
          document.createTextNode(String.valueOf(soundscribeConfiguration.getDefaultDivisions())));
      root.appendChild(divisions);

      for (Feature f : features.get(output)) {
        Element note = document.createElement("note");

        if (f.hasTimestamp) {
          Element timestamp = document.createElement("timestamp");
          timestamp.appendChild(
              document.createTextNode(TimeHelper.realTime2String(f.timestamp, 2)));
          note.appendChild(timestamp);
        } else {
          Element frame = document.createElement("frame");
          frame.appendChild(document.createTextNode(String.valueOf(frameTime)));
          note.appendChild(frame);
        }
        if (f.hasDuration) {
          Element duration = document.createElement("duration");
          duration.appendChild(document.createTextNode(TimeHelper.realTime2String(f.duration, 2)));
          note.appendChild(duration);
        }
        for (float v : f.values) {

          // frequency to midi
          midiValue = (int) (Math.round(69 + 12 * (Math.log(v / 440) / Math.log(2))));
          Element val = document.createElement("value");
          val.appendChild(document.createTextNode(String.valueOf(v)));
          note.appendChild(val);

          Element midi = document.createElement("midiValue");
          midi.appendChild(document.createTextNode(String.valueOf(midiValue)));
          note.appendChild(midi);

          Element letterNote = document.createElement("letterNote");
          letterNote.appendChild(
                  document.createTextNode(MidiNotes.getNoteSymbolByMidiValue(midiValue)));
          note.appendChild(letterNote);
        }

        root.appendChild(note);

        // create the xml file
        // transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(xmlFile);

        transformer.transform(domSource, streamResult);
      }
    } catch (ParserConfigurationException | TransformerException e) {
      log.error("Could not save result of pYIN algorithm to file", e);
    }
  }

  private void printSmoothedPitch(
          RealTime frameTime, Integer output, Map<Integer, List<Feature>> features, File file) {
    if (!features.containsKey(output)) {
      return;
    }
    try {
      PrintWriter writer = new PrintWriter(file, "UTF-8");
      // processing .wav file data
      for (Feature f : features.get(output)) {
        if (f.hasTimestamp) {
          writer.print(String.format("\n %s ", String.valueOf(f.timestamp)));
        } else {
          writer.print(String.format(" %s ", String.valueOf(frameTime)));
        }
        if (f.hasDuration) {
          writer.print(String.format(" %s ", String.valueOf(f.duration)));
        }
        for (float v : f.values) {

          writer.print(String.format(" %s ", v));
        }
      }
    } catch (Exception e) {
      log.error("Could not save result of pYIN algorithm to file", e);
    }
  }

  /**
   * Prints single fixed tempo to given file.
   *
   * @param features Features calculated by Vamp plugin
   * @param output   An identifier calculated by Vamp plugin
   * @param file     File to which the content is written to
   */
  private void printTempo(Map<Integer, List<Feature>> features, Integer output, File file) {
    try {
      PrintWriter writer = new PrintWriter(file, "UTF-8");

      for (Feature f : features.get(output)) {
        for (float v : f.values) {
          writer.println((int) v);
          writer.flush();
          writer.close();
          return;
        }
      }

    } catch (Exception e) {
      log.error("Could not save result of tempo estimator to file", e);
    }
  }

  private int readBlock(AudioFormat format, AudioInputStream stream, float[][] buffers)
      throws IOException {
    // 16-bit LE signed PCM only
    int channels = format.getChannels();
    byte[] raw = new byte[buffers[0].length * channels * 2];
    int read = stream.read(raw);
    if (read < 0) {
      return read;
    }
    int frames = read / (channels * 2);
    for (int i = 0; i < frames; ++i) {
      for (int c = 0; c < channels; ++c) {
        int ix = i * channels + c;
        int ival = (raw[ix * 2] & 0xff) | (raw[ix * 2 + 1] << 8);
        float fval = ival / 32768.0f;
        buffers[c][i] = fval;
      }
    }
    return frames;
  }

  /**
   * Estimates tempo using one of JVamp plugins.
   *
   * @param fileWav     WAV file
   * @param deleteAfter Should the file be treated as temporary
   * @return String with bpm, i.e. "129"
   */
  private String estimateTempo(File fileWav, boolean deleteAfter) {
    File tempoFile;
    String defaultBpm = "120";

    try {
      tempoFile = start(JvampFunctions.SIMPLE_FIXED_TEMPO_ESTIMATOR, fileWav);
    } catch (PyinConversionException e) {
      log.debug("Cannot run tempo estimator plugin", e);
      return defaultBpm;
    }
    String bpmRaw;

    try {
      InputStream is = new FileInputStream(tempoFile);
      bpmRaw = new BufferedReader(new InputStreamReader(is)).readLine();
    } catch (IOException e) {
      log.debug("Cannot read the output of tempo estimator plugin", e);
      return defaultBpm;
    }

    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        log.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }

    if (bpmRaw == null) {
      return defaultBpm;
    } else {
      return bpmRaw;
    }
  }
}
