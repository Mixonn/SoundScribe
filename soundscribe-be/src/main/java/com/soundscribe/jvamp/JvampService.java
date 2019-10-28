package com.soundscribe.jvamp;

import com.soundscribe.utilities.StaticVariables;

import java.io.File;

import static com.soundscribe.jvamp.JvampFunctions.NOTES;
import static com.soundscribe.jvamp.JvampFunctions.SMOOTHED_PITCH_TRACK;

public class JvampService {
    private Host host;

    public JvampService() {
        System.load(StaticVariables.LIB_PATH + "libvamp-hostsdk.so");
        System.load(StaticVariables.LIB_PATH + "libvamp-jni.so");
        host = new Host();
    }

    public void pyinSmoothedPitchTrack(File fileWav, boolean deleteAfter) {
        host.start(SMOOTHED_PITCH_TRACK, fileWav);
        fileWav.delete();
    }

    public void pyinNotes(File fileWav, boolean deleteAfter) {
        host.start(NOTES, fileWav);
        fileWav.delete();
    }
}
