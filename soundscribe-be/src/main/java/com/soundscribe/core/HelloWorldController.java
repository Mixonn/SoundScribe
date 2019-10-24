package com.soundscribe.core;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import com.soundscribe.conventer.ConventerService;
import com.soundscribe.jvamp.JvampService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloWorldController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        File file = new File("test.mp3");
        JvampService jvampService = new JvampService();
        ConventerService conventerService = new ConventerService();

        File wavFile = conventerService.convertMP3toWAV(file);
        System.out.println("Done");
        jvampService.pyinNotes(wavFile);
        return "ok";
    }

}