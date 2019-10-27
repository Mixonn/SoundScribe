package com.soundscribe.core;

import com.soundscribe.converter.ConverterService;
import com.soundscribe.jvamp.JvampService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
public class MainController {
    @GetMapping("/load")
    @ResponseBody
    public String test() {
        File file = new File("test.mp3");
        JvampService jvampService = new JvampService();
        ConverterService converterService = new ConverterService();
        File wavFile = converterService.convertMP3toWAV(file, false);
        jvampService.pyinNotes(wavFile, true);
        return "ok";
    }

    @GetMapping("/xmlToMidi")
    @ResponseBody
    public String getFoos(@RequestParam String filename) {
        File fileXML = new File(filename);
        ConverterService converterService = new ConverterService();
        converterService.convertXMLtoMIDI(fileXML, false);
        return "DONE";
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public String submit(@RequestParam("file") MultipartFile file, ModelMap modelMap) {
        modelMap.addAttribute("file", file);
        return "fileUploadView";
    }
}