package com.soundscribe.core;

import com.soundscribe.converter.ConverterService;
import com.soundscribe.jvamp.JvampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
public class MainController {

    @Autowired
    JvampService jvampService;
    @Autowired
    ConverterService converterService;

    @GetMapping("/file-processing/analyze-file")
    @ResponseBody
    public String analyzeFile(@RequestParam String filename) {
        jvampService.loadLibraries();
        File file = new File(filename);
        JvampService jvampService = new JvampService();
        File wavFile = converterService.convertMP3toWAV(file, false);
        jvampService.pyinNotes(wavFile, true);
        return "Plik z danymi wyściowymi został utworzony";
    }

    @GetMapping("/file-processing/xml-to-midi")
    @ResponseBody
    public String xmlToMidi(@RequestParam String filename) {
        jvampService.loadLibraries();
        File fileXML = new File(filename);
        ConverterService converterService = new ConverterService();
        converterService.convertXMLtoMIDI(fileXML, false);
        return "Plik midi został utworzony";
    }
}