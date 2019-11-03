package com.soundscribe.jvamp;

import com.soundscribe.utilities.StaticVariables;

import java.io.File;

import static com.soundscribe.jvamp.JvampFunctions.NOTES;
import static com.soundscribe.jvamp.JvampFunctions.SMOOTHED_PITCH_TRACK;

/**
 * Executes jVamp plugins.
 */
public class JvampService {
    private Host host;

    public JvampService() {
        System.load(StaticVariables.LIB_PATH + "libvamp-hostsdk.so");
        System.load(StaticVariables.LIB_PATH + "libvamp-jni.so");
        host = new Host();
    }

    /**
     *
     * @param fileWav Wav file for analysis.
     * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is completed.
     */
    public void pyinSmoothedPitchTrack(File fileWav, boolean deleteAfter) {
        host.start(SMOOTHED_PITCH_TRACK, fileWav);
        if(deleteAfter) fileWav.delete();
    }

    /**
     * Generetes xmlFile with timestamp,length and value of notes in song.
     * @param fileWav Wav file for analysis.
     * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is completed.
     */
    public void pyinNotes(File fileWav, boolean deleteAfter) {
        host.start(NOTES, fileWav);
        if(deleteAfter) fileWav.delete();
    }
}
