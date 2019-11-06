package com.soundscribe.jvamp;

import static com.soundscribe.jvamp.JvampFunctions.NOTES;
import static com.soundscribe.jvamp.JvampFunctions.SMOOTHED_PITCH_TRACK;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Executes jVamp plugins.
 */
@Component
public class JvampService {

  @Autowired
  private SoundscribeConfiguration soundscribeConfiguration;
  @Autowired
  public Host host;
  private static final Logger logger = LoggerFactory.getLogger(JvampService.class);

  public void loadLibraries() {
    System.load(soundscribeConfiguration.getSystemLib() + "libvamp-hostsdk.so");
    System.load(soundscribeConfiguration.getSystemLib() + "libvamp-jni.so");
  }

  /**
   * @param fileWav     Wav file for analysis.
   * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is
   *                    completed.
   */
  public void pyinSmoothedPitchTrack(File fileWav, boolean deleteAfter) {
    host.start(SMOOTHED_PITCH_TRACK, fileWav);
    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        logger.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }
  }

  /**
   * Generetes xmlFile with timestamp,length and value of notes in song.
   *
   * @param fileWav     Wav file for analysis.
   * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is
   *                    completed.
   */
  public void pyinNotes(File fileWav, boolean deleteAfter) {
    host.start(NOTES, fileWav);
    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        logger.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }
  }
}
