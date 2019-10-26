package com.soundscribe.converter;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;

public class ConverterService {

    public File convertMP3toWAV(File fileMp3) {
        String fileName = fileMp3.getName().split("\\.")[0];
        File fileWav = new File(fileName + ".wav");

        Converter converter = new Converter();
        try {
            converter.convert(fileMp3.getAbsolutePath(), fileWav.getAbsolutePath());
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
        return fileWav;
    }
}
