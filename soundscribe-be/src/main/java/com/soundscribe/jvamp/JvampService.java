package com.soundscribe.jvamp;

import java.io.File;
import java.util.Objects;

import static com.soundscribe.jvamp.Functions.NOTES;
import static com.soundscribe.jvamp.Functions.SMOOTHED_PITCH_TRACK;

public class JvampService {

    private static final String LIB_PATH = "/usr/lib/";
    private Host host;

    public JvampService() {
        System.load(LIB_PATH + "libvamp-hostsdk.so");
        System.load(LIB_PATH + "libvamp-jni.so");
        host = new Host();
    }

    public void pyinSmoothedPitchTrack(File file) {
        host.start(SMOOTHED_PITCH_TRACK, file);
    }

    public void pyinNotes(File file) {
        host.start(NOTES, file);
    }
}
