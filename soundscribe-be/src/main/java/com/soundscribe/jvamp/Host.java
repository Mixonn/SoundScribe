package com.soundscribe.jvamp;

import java.io.*;
import java.util.*;
import java.util.List;

import com.soundscribe.utilities.MidiNotes;
import org.vamp_plugins.PluginLoader;
import org.vamp_plugins.Plugin;
import org.vamp_plugins.OutputDescriptor;
import org.vamp_plugins.Feature;
import org.vamp_plugins.RealTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Host {

    private void printNotes(String filename, RealTime frameTime, Integer output,
                            Map<Integer, List<Feature>> features, File xmlFile) {
        int midiValue;

        if (!features.containsKey(output)) return;

        try {
            // Stuff for xml
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement(filename);
            document.appendChild(root);

            for (Feature f : features.get(output)) {
                Element note = document.createElement("note");

                if (f.hasTimestamp) {
                    Element timestamp = document.createElement("timestamp");
                    timestamp.appendChild(document.createTextNode(TimeHelper.RealTime2String(f.timestamp, 2)));
                    note.appendChild(timestamp);
                } else {
                    Element frame = document.createElement("frame");
                    frame.appendChild(document.createTextNode(String.valueOf(frameTime)));
                    note.appendChild(frame);
                }
                if (f.hasDuration) {
                    Element duration = document.createElement("duration");
                    duration.appendChild(document.createTextNode(TimeHelper.RealTime2String(f.duration, 2)));
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

                    Element letterNote = document.createElement("letterNotde");
                    letterNote.appendChild(document.createTextNode(MidiNotes.note(midiValue)));
                    note.appendChild(letterNote);
                }

                root.appendChild(note);

                // create the xml file
                //transform the DOM Object to an XML File
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource domSource = new DOMSource(document);
                StreamResult streamResult = new StreamResult(xmlFile);

                transformer.transform(domSource, streamResult);
            }
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
    }

    private void printSmoothedPitch(String filename, RealTime frameTime, Integer output,
                                    Map<Integer, List<Feature>> features, File file) throws IOException {
        if (!features.containsKey(output)) return;
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
            e.printStackTrace();
        }
    }

    private int readBlock(AudioFormat format, AudioInputStream stream,
                          float[][] buffers)
            throws IOException {
        // 16-bit LE signed PCM only
        int channels = format.getChannels();
        byte[] raw = new byte[buffers[0].length * channels * 2];
        int read = stream.read(raw);
        if (read < 0) return read;
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

    public void start(Functions function, File file) {
        File xmlFile = null;
        File smoothedFile = null;
        String key = null;
        PluginLoader loader = PluginLoader.getInstance();
        String fileName = file.getName().split("\\.")[0];
        switch (function) {
            case NOTES:
                key = "pyin:pyin:notes";
                xmlFile = new File(fileName + ".xml");
                break;
            case SMOOTHED_PITCH_TRACK:
                key = "pyin:pyin:smoothedpitchtrack";
                smoothedFile = new File(fileName + ".txt");
                break;
        }

        String[] keyparts = key.split(":");

        String pluginKey = keyparts[0] + ":" + keyparts[1];
        String outputKey = keyparts[2];

        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();

            if (format.getSampleSizeInBits() != 16 ||
                    format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED ||
                    format.isBigEndian()) {
                System.err.println("Sorry, only 16-bit signed little-endian PCM files supported");
                return;
            }

            float rate = format.getFrameRate();
            int channels = format.getChannels();
            int blockSize = 1024; // frames

            Plugin p = loader.loadPlugin
                    (pluginKey, rate, PluginLoader.AdapterFlags.ADAPT_ALL);

            OutputDescriptor[] outputs = p.getOutputDescriptors();
            int outputNumber = -1;
            for (int i = 0; i < outputs.length; ++i) {
                if (outputs[i].identifier.equals(outputKey)) outputNumber = i;
            }
            if (outputNumber < 0) {
                System.err.println("Plugin lacks output id: " + outputKey);
                System.err.print("Outputs are:");
                for (OutputDescriptor output : outputs) {
                    System.err.print(" " + output.identifier);
                }
                System.err.println("");
                return;
            }

            boolean b = p.initialise(channels, blockSize, blockSize);
            if (!b) {
                System.err.println("Plugin initialise failed");
                return;
            }

            float[][] buffers = new float[channels][blockSize];

            boolean done = false;
            boolean incomplete = false;
            int block = 0;

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
                        System.err.println("Audio file read incomplete! Short buffer detected at " + block * blockSize);
                        return;
                    }

                    incomplete = (read < buffers[0].length);

                    RealTime timestamp = RealTime.frame2RealTime
                            (block * blockSize, (int) (rate + 0.5));

                    p.process(buffers, timestamp);
                }

                ++block;
            }

            Map<Integer, List<Feature>>
                    features = p.getRemainingFeatures();

            RealTime timestamp = RealTime.frame2RealTime
                    (block * blockSize, (int) (rate + 0.5));
            switch (function) {
                case NOTES:
                    printNotes(fileName, timestamp, outputNumber, features, xmlFile);
                    break;
                case SMOOTHED_PITCH_TRACK:
                    printSmoothedPitch(fileName, timestamp, outputNumber, features, smoothedFile);
                    break;
            }

            p.dispose();
        } catch (IOException e) {
            System.err.println("Failed to read audio file: " + e.getMessage());

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + e.getMessage());

        } catch (PluginLoader.LoadFailedException e) {
            System.err.println("Plugin load failed (unknown plugin?): key is " + key);
        }
    }
}