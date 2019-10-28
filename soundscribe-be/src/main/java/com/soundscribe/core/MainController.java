package com.soundscribe.core;

import com.soundscribe.converter.ConverterService;
import com.soundscribe.jvamp.JvampService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

@Controller
public class MainController {
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        File file = new File("test.mp3");
        JvampService jvampService = new JvampService();
        ConverterService converterService = new ConverterService();
        File wavFile = converterService.convertMP3toWAV(file);
        jvampService.pyinNotes(wavFile);
        return "ok";
    }

    @GetMapping("/tempo/{filename}")
    @ResponseBody
    public String tempo(@PathVariable String filename) throws Exception {
        BeatDetector trackAnalyzerPort = new BeatDetector(filename);
        return trackAnalyzerPort.analyzeTrack().orElse("¯\\_(ツ)_/¯");
    }

}