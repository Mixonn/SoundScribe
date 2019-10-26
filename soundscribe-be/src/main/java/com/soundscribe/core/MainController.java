package com.soundscribe.core;

import java.io.File;

import com.soundscribe.converter.ConverterService;
import com.soundscribe.jvamp.JvampService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

}