package com.soundscribe.jvamp;

import com.soundscribe.utilities.SoundscribeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

import static com.soundscribe.jvamp.JvampFunctions.NOTES;
import static com.soundscribe.jvamp.JvampFunctions.SMOOTHED_PITCH_TRACK;

/**
 * Executes jVamp plugins.
 */
@Service
public class JvampService {
    @Autowired
    private SoundscribeConfiguration soundscribeConfiguration;
    @Autowired
    private Host host;

    public void loadLibraries() {
        System.load(soundscribeConfiguration.getSystemLib() + "libvamp-hostsdk.so");
        System.load(soundscribeConfiguration.getSystemLib() + "libvamp-jni.so");
    }

    /**
     * @param fileWav     Wav file for analysis.
     * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is completed.
     */
    public void pyinSmoothedPitchTrack(File fileWav, boolean deleteAfter) {
        host.start(SMOOTHED_PITCH_TRACK, fileWav);
        if (deleteAfter) fileWav.delete();
    }

    /**
     * Generetes xmlFile with timestamp,length and value of notes in song.
     *
     * @param fileWav     Wav file for analysis.
     * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is completed.
     */
    public void pyinNotes(File fileWav, boolean deleteAfter) {
        if(host==null) System.out.println("Nie ma hosta!");
        host.start(NOTES, fileWav);
        if (deleteAfter) fileWav.delete();
    }
}
