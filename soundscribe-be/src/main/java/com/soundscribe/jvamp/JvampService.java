package com.soundscribe.jvamp;

import org.vamp_plugins.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;
import java.util.Map;

public class JvampService {
    public void pyinNotes(File f) {
        {
            System.out.println("[pYIN] Start processing " + f.getName());
            File dylib = new File("libvamp-jni.dylib");
            System.load(dylib.getAbsolutePath());

            System.out.println("Załadowano");
            PluginLoader loader = PluginLoader.getInstance();

            File xmlFile = new File(f.getPath() + "1" + ".xml");

            String pluginKey = "pyin:pyin";
            String outputKey = "notes";

            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(f);
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

                Plugin p = loader.loadPlugin(pluginKey, rate, PluginLoader.AdapterFlags.ADAPT_ALL);

                OutputDescriptor[] outputs = p.getOutputDescriptors();
                int outputNumber = -1;
                for (int i = 0; i < outputs.length; ++i) {
                    if (outputs[i].identifier.equals(outputKey)) outputNumber = i;
                }
                if (outputNumber < 0) {
                    System.err.println("Plugin lacks output id: " + outputKey);
                    System.err.print("Outputs are:");
                    for (int i = 0; i < outputs.length; ++i) {
                        System.err.print(" " + outputs[i].identifier);
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

                        Map<Integer, List<Feature>>
                                features = p.process(buffers, timestamp);

                        printNotes("testestest", timestamp, outputNumber, features, xmlFile);
                    }

                    ++block;
                }

                Map<Integer, List<Feature>>
                        features = p.getRemainingFeatures();

                RealTime timestamp = RealTime.frame2RealTime
                        (block * blockSize, (int) (rate + 0.5));

                printNotes("testestest", timestamp, outputNumber, features, xmlFile);

                p.dispose();
                System.out.println("...Done!");
            } catch (java.io.IOException e) {
                System.err.println("Failed to read audio file: " + e.getMessage());

            } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
                System.err.println("Unsupported audio file format: " + e.getMessage());

            } catch (PluginLoader.LoadFailedException e) {
                System.err.println("Plugin load failed (unknown plugin?)");
            }
        }
    }

    private int readBlock(AudioFormat format, AudioInputStream stream,
                          float[][] buffers)
            throws java.io.IOException {
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

    private void printNotes(String filename, RealTime frameTime, Integer output,
                            Map<Integer, List<Feature>> features, File xmlFile) {
        int midiValue;

        if (!features.containsKey(output)) return;

        System.out.print("\tSaving xml file...");
        try {
            // Stuff for xml
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement(filename);
            document.appendChild(root);


            // processing .wav file data
            for (Feature f : features.get(output)) {
                Element note = document.createElement("note");

                if (f.hasTimestamp) {
                    Element timestamp = document.createElement("timestamp");
                    timestamp.appendChild(document.createTextNode(Helper.RealTime2String(f.timestamp, 2)));
                    note.appendChild(timestamp);
                } else {
                    Element frame = document.createElement("frame");
                    frame.appendChild(document.createTextNode(String.valueOf(frameTime)));
                    note.appendChild(frame);
                }
                if (f.hasDuration) {
                    Element duration = document.createElement("duration");
                    duration.appendChild(document.createTextNode(Helper.RealTime2String(f.duration, 2)));
                    note.appendChild(duration);
                }
                for (float v : f.values) {

                    // Zamiana częstotliwości na wartość nuty w midi
                    // https://stackoverflow.com/questions/27357727/calcute-note-based-on-frequency
                    midiValue = (int) (Math.round(69 + 12 * (Math.log(v / 440) / Math.log(2))));

                    Element val = document.createElement("value");
                    val.appendChild(document.createTextNode(String.valueOf(v)));
                    note.appendChild(val);

                    Element midi = document.createElement("midiValue");
                    midi.appendChild(document.createTextNode(String.valueOf(midiValue)));
                    note.appendChild(midi);

                    Element letterNote = document.createElement("letterNote");
                    letterNote.appendChild(document.createTextNode(Notes.note(midiValue)));
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
            System.out.println(" OK!");
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }
}
