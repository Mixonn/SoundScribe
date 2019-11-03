package com.soundscribe.converter;

/**
 * Stores detailed data about notes.
 */
public class NotePojo {
    double timestamp;
    double duration;
    double value;
    int midiValue;
    String letterNote;

    public NotePojo(double timestamp, double duration, double value, int midiValue, String letterNote) {
        this.timestamp = timestamp;
        this.duration = duration;
        this.value = value;
        this.midiValue = midiValue;
        this.letterNote = letterNote;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getMidiValue() {
        return midiValue;
    }

    public void setMidiValue(int midiValue) {
        this.midiValue = midiValue;
    }

    public String getLetterNote() {
        return letterNote;
    }

    public void setLetterNote(String letterNote) {
        this.letterNote = letterNote;
    }
}
