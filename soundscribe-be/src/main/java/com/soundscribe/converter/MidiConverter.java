package com.soundscribe.converter;

import com.soundscribe.utilities.StaticVariables;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.sound.midi.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiConverter {
    public File convertXMLtoMidi(File fileXML){
        File midiFile = null;
        int PPQ = 24;
        int BPM = 120; //120 standard midi BPM //todo take value from orginal song
        XmlPojo xml = readXMLData(fileXML);

        try {
            Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
            Track track = sequence.createTrack();
            // Adding notes
            int tickToStart, ticksToStop;
            for (NotePojo note : xml.getNotes()) {
                // Add Note On event
                tickToStart = secondsToTicks(BPM, PPQ, note.timestamp);
                track.add(makeEvent(144, 1, note.midiValue, 96, tickToStart));
                // Add Note Off event
                ticksToStop = secondsToTicks(BPM, PPQ, note.duration);
                track.add(makeEvent(128, 1, note.midiValue, 96, tickToStart + ticksToStop));
            }

            //write MIDI
            midiFile = new File(StaticVariables.SONG_DATA_STORAGE_PATCH + xml.getName() + ".mid");
            MidiSystem.write(sequence, 1, midiFile);

        } catch (Exception e) {
            System.out.println("Error while creating MIDI file.");
            e.printStackTrace();
        }
        return midiFile;
    }

    private MidiEvent makeEvent(int command, int channel, int note, int velocity, int tick) {

        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(command, channel, note, velocity);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return event;
    }

    private int secondsToTicks(int BPM, int PPQ, double time) {
        double tickTimeInMs = (double) 60000 / (BPM * PPQ);
        return (int) (time * 1000 / tickTimeInMs);
    }

    private XmlPojo readXMLData(File fileXML) {
        XmlPojo xmlPojo = new XmlPojo();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println("Error while converting XML.");
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = builder.parse(fileXML);
        } catch (SAXException e) {
            System.out.println("Invalid xml file format");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not find xml file.");
            e.printStackTrace();
        }
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        String songName = root.getNodeName();
        xmlPojo.setName(songName);

        List<NotePojo> noteList = new ArrayList<>();
        NodeList nList = document.getElementsByTagName("note");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                double timestamp = Double.parseDouble(eElement.getElementsByTagName("timestamp").item(0).getTextContent());
                double duration = Double.parseDouble(eElement.getElementsByTagName("duration").item(0).getTextContent());
                double value = Double.parseDouble(eElement.getElementsByTagName("value").item(0).getTextContent());
                int midiValue = Integer.parseInt(eElement.getElementsByTagName("midiValue").item(0).getTextContent());
                String letterNotde = eElement.getElementsByTagName("letterNote").item(0).getTextContent();
                noteList.add(new NotePojo(timestamp, duration, value, midiValue, letterNotde));
            }
        }
        xmlPojo.setNotes(noteList);
        return xmlPojo;
    }
}
