package com.soundscribe.jvamp;

import static com.soundscribe.jvamp.JvampFunctions.NOTES;
import static com.soundscribe.jvamp.JvampFunctions.SMOOTHED_PITCH_TRACK;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Executes jVamp plugins.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JvampService {
  private final SoundscribeConfiguration soundscribeConfiguration;
  private final Host host;

  public void loadLibraries() {
    System.load(soundscribeConfiguration.getVampPath() + "libvamp-hostsdk.so");
    System.load(soundscribeConfiguration.getVampPath() + "libvamp-jni.so");
  }

  /**
   * @param fileWav     Wav file for analysis.
   * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is
   *                    completed.
   */
  public File pyinSmoothedPitchTrack(File fileWav, boolean deleteAfter)
      throws PyinConversionException {
    File resultFile = host.start(SMOOTHED_PITCH_TRACK, fileWav);
    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        log.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }
    return resultFile;
  }

  /**
   * Generetes xmlFile with timestamp,length and value of notes in song.
   *
   * @param fileWav     Wav file for analysis.
   * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is
   *                    completed.
   */
  public File pyinNotes(File fileWav, boolean deleteAfter) throws PyinConversionException {
    File xmlFile = host.start(NOTES, fileWav);
    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        log.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }
    return xmlFile;
  }
}
