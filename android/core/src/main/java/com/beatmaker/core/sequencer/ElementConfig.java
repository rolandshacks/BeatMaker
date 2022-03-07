package com.beatmaker.core.sequencer;

import com.beatmaker.config.Constants;
import com.beatmaker.core.midi.MidiNote;

import java.util.ArrayList;
import java.util.List;

public class ElementConfig {

    private int channel;
    private List<MidiNote> notes;

    public ElementConfig() {
        channel = Constants.DEFAULT_MIDI_CHANNEL;
        setNote(channel, Constants.DEFAULT_NOTE_PITCH, Constants.DEFAULT_NOTE_VELOCITY);
    }

    public ElementConfig(ElementConfig config) {
        channel = config.channel;
        if (null != config.notes) {
            for (MidiNote note : config.notes) {
                addNote(new MidiNote(channel, note.getPitch(), note.getVelocity()));
            }
        }
    }

    public void reset() {
        channel = Constants.DEFAULT_MIDI_CHANNEL;
        setNote(Constants.DEFAULT_MIDI_CHANNEL, Constants.DEFAULT_NOTE_PITCH, Constants.DEFAULT_NOTE_VELOCITY);
    }

    public void clearNotes() {
        notes = null;
    }

    public void setNote(int channel, int pitch, int velocity) {
        setNote(new MidiNote(channel, pitch, velocity));
    }

    public void setNote(MidiNote note) {
        clearNotes();
        addNote(note);
    }

    public void setNotes(MidiNote[] notes) {
        clearNotes();
        if (null != notes) {
            addNotes(notes);
        }
    }

    public void setNotes(List<MidiNote> notes) {
        clearNotes();
        if (null != notes) {
            addNotes(notes);
        }
    }

    public void addNotes(MidiNote[] notes) {
        if (null != notes) {
            for (MidiNote note : notes) {
                addNote(note);
            }
        }
    }

    public void addNotes(List<MidiNote> notes) {
        if (null != notes) {
            for (MidiNote note : notes) {
                addNote(note);
            }
        }
    }

    public void addNote(int channel, int pitch, int velocity) {
        addNote(new MidiNote(channel, pitch, velocity));
    }

    public void addNote(MidiNote note) {
        if (null == notes) {
            notes = new ArrayList<>();
        }
        notes.add(note);
    }

    public List<MidiNote> getNotes() {
        return notes;
    }

    public boolean hasNotes() {
        return (null != notes && notes.size() > 0);
    }

    public boolean hasSingleNote() {
        return (null != notes && notes.size() == 1);
    }

    public MidiNote getSingleNote() {
        if (null != notes && notes.size() == 1) return notes.get(0);
        return null;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
