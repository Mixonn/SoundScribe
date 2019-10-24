package com.soundscribe.conventer;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;
import java.io.IOException;

public class ConventerService {

    public File convertMP3toWAV(File fileMp3) {
        File temp = null;
        try {
            temp = File.createTempFile("temp_" + fileMp3.getName().split("\\.")[0], ".wav");
            temp.deleteOnExit();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Converter converter = new Converter();
        try {
            converter.convert(fileMp3.getAbsolutePath(), temp.getAbsolutePath());
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
