package com.soundscribe.converter;

import java.util.List;

public class XmlPojo {
    private String name;
    private List<NotePojo> notes;

    public XmlPojo() {
    }

    public XmlPojo(String name, List<NotePojo> notes, Double timestamp, Double duration, Double value, Integer midiValue, String letterNotde) {
        this.name = name;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NotePojo> getNotes() {
        return notes;
    }

    public void setNotes(List<NotePojo> notes) {
        this.notes = notes;
    }
}
