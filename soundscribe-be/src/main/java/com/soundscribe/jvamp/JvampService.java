package com.soundscribe.jvamp;

import java.io.File;
import java.util.Objects;

import static com.soundscribe.jvamp.Functions.NOTES;
import static com.soundscribe.jvamp.Functions.SMOOTHED_PITCH_TRACK;

public class JvampService {

    private Host host;

    public JvampService() {
        File libvampHOST = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("libvamp-hostsdk.so")).getFile());
        File libvampJNI = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("libvamp-jni.so")).getFile());
        System.load(libvampHOST.getAbsolutePath());
        System.load(libvampJNI.getAbsolutePath());
        host = new Host();
    }

    public void pyinSmoothedPitchTrack(File file) {
        host.start(SMOOTHED_PITCH_TRACK, file);
    }

    public void pyinNotes(File file) {
        host.start(NOTES, file);
    }
}
