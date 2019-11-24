package com.soundscribe.jvamp;

import com.soundscribe.utilities.SoundscribeConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.soundscribe.jvamp.JvampFunctions.NOTES;
import static com.soundscribe.jvamp.JvampFunctions.SMOOTHED_PITCH_TRACK;

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
   * @param fileWav Wav file for analysis.
   * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is
   *     completed.
   */
  public void pyinSmoothedPitchTrack(File fileWav, File fileMp3, boolean deleteAfter) {
    host.start(SMOOTHED_PITCH_TRACK, fileWav, fileMp3);
    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        log.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }
  }

  /**
   * Generetes xmlFile with timestamp,length and value of notes in song.
   *
   * @param fileWav Wav file for analysis.
   * @param deleteAfter Boolean, if set on true wav file will be deleted after the process is
   *     completed.
   */
  public void pyinNotes(File fileWav, File fileMp3, boolean deleteAfter) {
    host.start(NOTES, fileWav, fileMp3);
    if (deleteAfter) {
      try {
        Files.delete(fileWav.toPath());
      } catch (IOException e) {
        log.debug("The wav file cannot be deleted. This file no longer exists.");
      }
    }
  }
}
