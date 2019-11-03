package com.soundscribe.converter;

import com.soundscribe.utilities.StaticVariables;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;

public class ConverterService {
    public File convertMP3toWAV(File fileMp3, boolean deleteAfter) {
        String fileName = fileMp3.getName().split("\\.")[0];
        File fileWav = new File(StaticVariables.SONG_DATA_STORAGE_PATCH + fileName + ".wav");
        Converter converter = new Converter();

        try {
            converter.convert(fileMp3.getAbsolutePath(), fileWav.getAbsolutePath());
        } catch (JavaLayerException e) {
            System.out.println("An error occurred while converting mp3 to wav");
        }

        if(deleteAfter) fileMp3.delete();
        return fileWav;
    }

    public File convertXMLtoMIDI(File fileXML, boolean deleteAfter) {
        MidiConverter midiConverter = new MidiConverter();
        File midi = midiConverter.convertXmlToMidi(fileXML);
        if(deleteAfter) fileXML.delete();
        return midi;
    }
}
