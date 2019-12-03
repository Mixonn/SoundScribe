package com.soundscribe.core;

import TrackAnalyzer.AudioData;
import TrackAnalyzer.KeyDetectionResult;
import TrackAnalyzer.KeyFinder;
import at.ofai.music.beatroot.BeatRoot;
import it.sauronsoftware.jave.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/* This class is a slight modification from tfriedel/trackanalyzer project from GitHub.
 * Original source: https://github.com/tfriedel/trackanalyzer/blob/master/TrackAnalyzer/TrackAnalyzer.java
 */
@Component
public class BeatDetector {

  /**
   * Decodes an audio file (mp3, flac, wav, etc. everything which can be decoded by ffmpeg) to a
   * downsampled wav file.
   *
   * @param input an audio file which will be decoded to wav
   * @param wavoutput the output wav file
   * @throws IllegalArgumentException
   * @throws InputFormatException
   * @throws EncoderException
   */
  private static void decodeAudioFile(File input, File wavoutput)
      throws IllegalArgumentException, InputFormatException, EncoderException {
    decodeAudioFile(input, wavoutput, 4410);
  }

  /**
   * Decodes an audio file (mp3, flac, wav, etc. everything which can be decoded by ffmpeg) to a
   * downsampled wav file.
   *
   * @param input an audio file which will be decoded to wav
   * @param wavoutput the output wav file
   * @param samplerate the samplerate of the output wav.
   * @throws IllegalArgumentException
   * @throws InputFormatException
   * @throws EncoderException
   */
  private static void decodeAudioFile(File input, File wavoutput, int samplerate)
      throws IllegalArgumentException, InputFormatException, EncoderException {
    assert wavoutput.getName().endsWith(".wav");
    AudioAttributes audio = new AudioAttributes();
    audio.setCodec("pcm_s16le");
    audio.setChannels(Integer.valueOf(1));
    audio.setSamplingRate(new Integer(samplerate));
    EncodingAttributes attrs = new EncodingAttributes();
    attrs.setFormat("wav");
    attrs.setAudioAttributes(audio);
    Encoder encoder = new Encoder();
    encoder.encode(input, wavoutput, attrs);
  }

  /**
   * runs key and bpm detector on
   *
   * @return
   * @filename, optionally writes tags
   */
  public Integer analyzeTrack(File input) {
    KeyFinder k = new KeyFinder();
    String wavfilename = "";
    AudioData data = new AudioData();
    File temp = null;
    File temp2 = null;
    synchronized (this) {
      try {

        temp = File.createTempFile("keyfinder", ".wav");
        temp2 = File.createTempFile("keyfinder2", ".wav");
        wavfilename = temp.getAbsolutePath();
        // Delete temp file when program exits.
        temp.deleteOnExit();
        temp2.deleteOnExit();
        if (input.getAbsolutePath().toLowerCase().endsWith((".wav"))) {
          decodeAudioFile(input, temp2);
        } else {
          decodeAudioFile(input, temp, 44100);
          decodeAudioFile(temp, temp2);
        }
      } catch (Exception ex) {
        Logger.getLogger(BeatDetector.class.getName())
                .log(Level.WARNING, "error while decoding" + input.getName() + ".");
        if (temp.length() == 0) {
          temp.delete();
          temp2.delete();
          return null;
        }
      }
    }
    KeyDetectionResult r;
    try {
      synchronized (this) {
        data.loadFromAudioFile(temp2.getAbsolutePath());
      }
    } catch (Exception ex) {
      Logger.getLogger(BeatDetector.class.getName()).log(Level.SEVERE, null, ex);
      deleteTempFiles(temp, temp2);
      return null;
    }

    // get bpm
    double bpm = BeatRoot.getBPM(wavfilename);
    if (Double.isNaN(bpm)) {
      try {
        // bpm couldn't be detected. try again with a higher quality wav.
        Logger.getLogger(BeatDetector.class.getName())
                .log(
                        Level.WARNING,
                        "bpm couldn't be detected for " + input.getName() + ". Trying again.");
        decodeAudioFile(input, temp, 44100);
        bpm = BeatRoot.getBPM(wavfilename);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (Double.isNaN(bpm)) {
      Logger.getLogger(BeatDetector.class.getName())
              .log(Level.WARNING, "bpm still couldn't be detected for " + input.getName() + ".");
    } else {
      Logger.getLogger(BeatDetector.class.getName())
              .log(Level.INFO, "bpm now detected correctly for " + input.getName());
    }

    deleteTempFiles(temp, temp2);

    if (!Double.isNaN(bpm)) {
      return (int) bpm;
    } else {
      return null;
    }
  }

  private void deleteTempFiles(File temp, File temp2) {
    if (temp != null) {
      temp.delete();
    }
    if (temp2 != null) {
      temp2.delete();
    }
  }
}
