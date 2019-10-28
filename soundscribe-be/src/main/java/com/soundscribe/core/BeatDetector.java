package com.soundscribe.core;

import TrackAnalyzer.AudioData;
import TrackAnalyzer.KeyDetectionResult;
import TrackAnalyzer.KeyFinder;
import at.ofai.music.beatroot.BeatRoot;
import it.sauronsoftware.jave.*;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/* This class is a slight modification from tfriedel/trackanalyzer project from GitHub.
 * Original source: https://github.com/tfriedel/trackanalyzer/blob/master/TrackAnalyzer/TrackAnalyzer.java
 */
public class BeatDetector {

    private String filename;

    public BeatDetector(String filename) throws Exception {
        this.filename = filename;

    }

    /**
     * Decodes an audio file (mp3, flac, wav, etc. everything which can be
     * decoded by ffmpeg) to a downsampled wav file.
     *
     * @param input     an audio file which will be decoded to wav
     * @param wavoutput the output wav file
     * @throws IllegalArgumentException
     * @throws InputFormatException
     * @throws EncoderException
     */
    public static void decodeAudioFile(File input, File wavoutput) throws IllegalArgumentException, InputFormatException, EncoderException {
        decodeAudioFile(input, wavoutput, 4410);
    }

    /**
     * Decodes an audio file (mp3, flac, wav, etc. everything which can be
     * decoded by ffmpeg) to a downsampled wav file.
     *
     * @param input      an audio file which will be decoded to wav
     * @param wavoutput  the output wav file
     * @param samplerate the samplerate of the output wav.
     * @throws IllegalArgumentException
     * @throws InputFormatException
     * @throws EncoderException
     */
    public static void decodeAudioFile(File input, File wavoutput, int samplerate) throws IllegalArgumentException, InputFormatException, EncoderException {
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
    public Optional<String> analyzeTrack() {
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
                if (filename.toLowerCase().endsWith((".wav"))) {
                    decodeAudioFile(new File(filename), temp2);
                } else {
                    decodeAudioFile(new File(filename), temp, 44100);
                    decodeAudioFile(temp, temp2);
                }
            } catch (Exception ex) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.WARNING, "error while decoding" + filename + ".");
                if (temp.length() == 0) {
                    temp.delete();
                    temp2.delete();
                    return Optional.empty();
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
            return Optional.empty();
        }

        String formattedBpm = "0";
        if (true) {
            // get bpm
            double bpm = BeatRoot.getBPM(wavfilename);
            if (Double.isNaN(bpm)) {
                try {
                    // bpm couldn't be detected. try again with a higher quality wav.
                    Logger.getLogger(BeatDetector.class.getName()).log(Level.WARNING, "bpm couldn't be detected for " + filename + ". Trying again.");
                    decodeAudioFile(new File(filename), temp, 44100);
                    bpm = BeatRoot.getBPM(wavfilename);
                    if (Double.isNaN(bpm)) {
                        Logger.getLogger(BeatDetector.class.getName()).log(Level.WARNING, "bpm still couldn't be detected for " + filename + ".");
                    } else {
                        Logger.getLogger(BeatDetector.class.getName()).log(Level.INFO, "bpm now detected correctly for " + filename);
                    }
                } catch (Exception ex) {
                    return Optional.empty();
                }
            } else if (Double.isNaN(bpm)) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.WARNING, "bpm couldn't be detected for " + filename + ".");
            }
            if (!Double.isNaN(bpm)) {
                formattedBpm = new DecimalFormat("#.#").format(bpm).replaceAll(",", ".");
            }
        }

        deleteTempFiles(temp, temp2);
        return Optional.of(formattedBpm);
    }

    private void deleteTempFiles(File temp, File temp2) {
        if (temp != null) {
            temp.delete();
        }
        if (temp2 != null) {
            temp2.delete();
        }
    }

    /**
     * This will write a custom ID3 tag (TXXX). This works only with MP3 files
     * (Flac with ID3-Tag not tested).
     *
     * @param description The description of the custom tag i.e. "catalognr"
     *                    There can only be one custom TXXX tag with that description in one MP3
     *                    file
     * @param text        The actual text to be written into the new tag field
     * @return True if the tag has been properly written, false otherwise
     */
    public static boolean setCustomTag(AudioFile audioFile, String description, String text) throws IOException {
        FrameBodyTXXX txxxBody = new FrameBodyTXXX();
        txxxBody.setDescription(description);
        txxxBody.setText(text);

        // Get the tag from the audio file
        // If there is no ID3Tag create an ID3v2.3 tag
        Tag tag = audioFile.getTagOrCreateAndSetDefault();
        if (tag instanceof AbstractID3Tag) {
            // If there is only a ID3v1 tag, copy data into new ID3v2.3 tag
            if (!(tag instanceof ID3v23Tag || tag instanceof ID3v24Tag)) {
                Tag newTagV23 = null;
                if (tag instanceof ID3v1Tag) {
                    newTagV23 = new ID3v23Tag((ID3v1Tag) audioFile.getTag()); // Copy old tag data
                }
                if (tag instanceof ID3v22Tag) {
                    newTagV23 = new ID3v23Tag((ID3v22Tag) audioFile.getTag()); // Copy old tag data
                }
                audioFile.setTag(newTagV23);
                tag = newTagV23;
            }

            AbstractID3v2Frame frame = null;
            if (tag instanceof ID3v23Tag) {
                if (((ID3v23Tag) audioFile.getTag()).getInvalidFrames() > 0) {
                    throw new IOException("read some invalid frames!");
                }
                frame = new ID3v23Frame("TXXX");
            } else if (tag instanceof ID3v24Tag) {
                if (((ID3v24Tag) audioFile.getTag()).getInvalidFrames() > 0) {
                    throw new IOException("read some invalid frames!");
                }
                frame = new ID3v24Frame("TXXX");
            }

            frame.setBody(txxxBody);

            try {
                tag.setField(frame);
            } catch (FieldDataInvalidException e) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        } else if (tag instanceof FlacTag) {
            try {
                ((FlacTag) tag).setField(description, text);
            } catch (KeyNotFoundException ex) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (FieldDataInvalidException ex) {
                return false;
            }
        } else if (tag instanceof Mp4Tag) {
            //TagField field = new Mp4TagTextField("----:com.apple.iTunes:"+description, text);
            TagField field;
            field = new Mp4TagReverseDnsField(Mp4TagReverseDnsField.IDENTIFIER
                    + ":" + "com.apple.iTunes" + ":" + description,
                    "com.apple.iTunes", description, text);
            //TagField field = new Mp4TagTextField(description, text);
            try {
                tag.setField(field);
            } catch (FieldDataInvalidException ex) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else if (tag instanceof VorbisCommentTag) {
            try {
                ((VorbisCommentTag) tag).setField(description, text);
            } catch (KeyNotFoundException ex) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (FieldDataInvalidException ex) {
                Logger.getLogger(BeatDetector.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            // tag not implented
            Logger.getLogger(BeatDetector.class.getName()).log(Level.WARNING, "couldn't write key information for " + audioFile.getFile().getName() + " to tag, because this format is not supported.");
            return false;
        }

        // write changes in tag to file
        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}